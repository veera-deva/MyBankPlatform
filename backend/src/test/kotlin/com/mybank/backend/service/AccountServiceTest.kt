package com.mybank.backend.service

import com.mybank.backend.domain.Customer
import com.mybank.backend.repository.CustomerRepository
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(AccountService::class)
class AccountServiceTest {

    companion object {
        @Container @ServiceConnection val postgres = PostgreSQLContainer("postgres:16")
    }

    @Autowired lateinit var customerRepository: CustomerRepository

    @Autowired lateinit var accountService: AccountService

    @Test
    fun `second account for same customer is rejected`() {
        val customer =
                customerRepository.save(
                        Customer(
                                email = "dub-account@example.com",
                                passwordHash = "hash",
                                fullName = "Test User"
                        )
                )
        accountService.createAccount(customer.id!!)
        assertFailsWith<AccountAlreadyExistException> {
            accountService.createAccount(customer.id!!)
        }
    }
}
