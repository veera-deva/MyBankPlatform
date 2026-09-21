package com.mybank.backend

import com.mybank.backend.domain.Account
import com.mybank.backend.domain.AccountStatus
import com.mybank.backend.domain.Customer
import com.mybank.backend.domain.LedgerEntry
import com.mybank.backend.domain.TransactionType
import com.mybank.backend.repository.AccountRepository
import com.mybank.backend.repository.CustomerRepository
import com.mybank.backend.repository.LedgerEntryRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class RepositoryIntegrationTest {

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

    @Test
    fun `persists and reads back a customer, account, and ledger entry`() {
        val customer = customerRepository.save(
            Customer(
                email = "test@example.com",
                passwordHash = "hashed-password",
                fullName = "Test User"
            )
        )
        assertNotNull(customer.id)

        val account = accountRepository.save(
            Account(
                customer = customer,
                accountNumber = "ACC-0001"
            )
        )
        assertEquals(AccountStatus.ACTIVE, account.status)

        val entry = ledgerEntryRepository.save(
            LedgerEntry(
                account = account,
                type = TransactionType.DEPOSIT,
                amount = BigDecimal("100.00"),
                balanceAfter = BigDecimal("100.00")
            )
        )

        val reloaded = ledgerEntryRepository.findById(entry.id!!).orElseThrow()
        assertEquals(BigDecimal("100.00"), reloaded.amount)
        assertEquals(TransactionType.DEPOSIT, reloaded.type)
    }
}
