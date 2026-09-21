
package com.mybank.backend.service

import com.mybank.backend.domain.Account
import com.mybank.backend.domain.Customer
import com.mybank.backend.repository.AccountRepository
import com.mybank.backend.repository.CustomerRepository
import com.mybank.backend.repository.LedgerEntryRepository
import com.mybank.backend.service.InsufficientFundsException
import com.mybank.backend.service.LedgerService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.Commit
import org.springframework.test.context.transaction.TestTransaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(LedgerService::class)
class LedgerServiceTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:16")
    }


    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var ledgerEntryRepository: LedgerEntryRepository

    @Autowired
    lateinit var ledgerService: LedgerService

    private fun seedAccount(email: String): Account {

        val customer = customerRepository.save(Customer(email = email, passwordHash = "hash", fullName = "Test User"))
        return accountRepository.save(Account(customer = customer, accountNumber = "ACC-${System.nanoTime()}"))
    }


    @Test
    fun `deposit and withdrawal update balance correctly`() {
        val account = seedAccount("balance-test@example.com")

        ledgerService.deposit(account.id!!, BigDecimal("100.00"))
        assertEquals(BigDecimal("100.00"), ledgerService.currentBalance(account.id!!))

        ledgerService.withdraw(account.id!!, BigDecimal("30.00"))
        assertEquals(BigDecimal("70.00"), ledgerService.currentBalance(account.id!!))
    }

    @Test
    fun `withdrawal exceeding balance is rejected`() {
        val account = seedAccount("insufficient-test@example.com")
        ledgerService.deposit(account.id!!, BigDecimal("50.00"))
        assertFailsWith<InsufficientFundsException> {
            ledgerService.withdraw(account.id!!, BigDecimal("50.01"))
        }
        assertEquals(BigDecimal("50.00"), ledgerService.currentBalance(account.id!!))
    }

    @Test
    fun `concurrent withdrawals never overdraw the account`() {
        val account = seedAccount("concurrency-test@example.com")
        ledgerService.deposit(account.id!!, BigDecimal("100.00"))
        TestTransaction.flagForCommit()
        TestTransaction.end()
        val executor = Executors.newFixedThreadPool(2)
        val readyLatch = CountDownLatch(2)
        val startLatch = CountDownLatch(1)

        val results = List(2) {
            executor.submit<Boolean> {
                readyLatch.countDown()
                startLatch.await()
                try {
                    ledgerService.withdraw(account.id!!, BigDecimal("80.00"))
                    true
                } catch (e: InsufficientFundsException) {
                    false
                }
            }
        }
        readyLatch.await()
        startLatch.countDown()
        val outcomes = results.map { it.get(10, TimeUnit.SECONDS) }
        executor.shutdown()

        assertEquals(1, outcomes.count { it }, "Exactly one \$80 withdrawal should succeed against a \$100 balance")
        TestTransaction.start()
        val finalBalance = ledgerService.currentBalance(account.id!!)
        assertTrue(finalBalance >= BigDecimal.ZERO, "Balance must never go negative")
        assertEquals(BigDecimal("20.00"), finalBalance)

    }

}