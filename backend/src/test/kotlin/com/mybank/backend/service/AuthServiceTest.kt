package com.mybank.backend.service

import com.mybank.backend.TEST_JWT_SECRET
import com.mybank.backend.domain.Customer
import com.mybank.backend.repository.CustomerRepository
import com.mybank.backend.security.JwtService
import com.mybank.backend.web.dto.LoginRequest
import com.mybank.backend.web.dto.RegisterRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.test.context.TestPropertySource

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(AuthService::class, JwtService::class, AuthServiceTest.TestConfig::class)
@TestPropertySource(properties = ["app.jwt.secret=$TEST_JWT_SECRET"])
class AuthServiceTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:16")

        // The one customer every test can rely on already existing.
        // Kept separate from the "register a brand-new customer" test below, which
        // deliberately uses a different, never-seeded email.
        private const val EXISTING_EMAIL = "existing@example.com"
        private const val EXISTING_PASSWORD = "password123"
    }


    // Provides just what AuthService needs, not the whole application context
    // Avoid pulling in the whole SecurityConfig/filter chain, etc.
    class TestConfig {
        @Bean
        fun passwordEncoder(): PasswordEncoder {
            return BCryptPasswordEncoder()
        }
    }

    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Autowired
    lateinit var authService: AuthService

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    // @DataJpaTest rolls back each test method's transaction independently, so no test
    // can rely on data left behind by another. Every test must set up its own fixture -
    // this seeds the one "already exists" customer that most of these tests need.
    @BeforeEach
    fun seedExistingCustomer() {
        customerRepository.save(
            Customer(
                email = EXISTING_EMAIL,
                passwordHash = passwordEncoder.encode(EXISTING_PASSWORD),
                fullName = "Existing User"
            )
        )
    }

    @Test
    fun `register should create a new customer and return a token`() {
        // Deliberately a different email from EXISTING_EMAIL: this test needs an email
        // that does NOT already exist, which is the opposite of what most other tests need.
        val request = RegisterRequest(
            email = "newcustomer@example.com",
            password = "password123",
            fullName = "New Customer"
        )
        assertDoesNotThrow {
            val response = authService.register(request)
            assertTrue(response.token.isNotEmpty(), "Token should not be empty")
        }
    }

    @Test
    fun `register rejects a duplicate email`() {
        val request = RegisterRequest(
            email = EXISTING_EMAIL,
            password = "someOtherPassword",
            fullName = "Someone Else"
        )
        assertThrows(EmailAlreadyRegisteredException::class.java) {
            authService.register(request)
        }
    }

    @Test
    fun `login with valid credentials returns a token`() {
        val request = LoginRequest(
            email = EXISTING_EMAIL,
            password = EXISTING_PASSWORD
        )
        assertDoesNotThrow {
            val response = authService.login(request)
            assertTrue(response.token.isNotEmpty(), "Token should not be empty")
        }
    }

    @Test
    fun `login with invalid credentials throws exception`() {
        // Uses EXISTING_EMAIL with the WRONG password, so this actually exercises the
        // password-mismatch branch of AuthService.login - not just the "no such user" branch.
        val request = LoginRequest(
            email = EXISTING_EMAIL,
            password = "wrongpassword"
        )
        assertThrows(InvalidCredentialsException::class.java) {
            authService.login(request)
        }
    }
}
