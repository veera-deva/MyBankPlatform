package com.mybank.backend.service

import com.mybank.backend.TEST_JWT_SECRET
import com.mybank.backend.repository.CustomerRepository
import com.mybank.backend.security.JwtService
import com.mybank.backend.web.dto.LoginRequest
import com.mybank.backend.web.dto.RegisterRequest
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(AuthService::class, JwtService::class, AuthServiceTest.TestConfig::class)
@TestPropertySource(properties = ["app.jwt.secret=$TEST_JWT_SECRET"])
class AuthServiceTest {

    companion object {
        @Container @ServiceConnection val postgres = PostgreSQLContainer("postgres:16")
    }

    // Provides just what AuthService needs, not the whole application context
    // Avoid pulling in the whole SecurityConfig/filter chain, etc.
    class TestConfig {
        @Bean
        fun passwordEncoder(): PasswordEncoder {
            return BCryptPasswordEncoder()
        }
    }

    @Autowired lateinit var customerRepository: CustomerRepository

    @Autowired lateinit var authService: AuthService

    @Test
    fun `register should create a new customer and return a token`() {
        val request =
                RegisterRequest(
                        email = "register-new@example.com",
                        password = "password",
                        "Test User"
                )
        // Test implementation here
        assertDoesNotThrow {
            val response = authService.register(request)
            assertTrue(response.token.isNotEmpty(), "Token should not be empty")
        }
    }

    @Test
    fun `register rejects a duplicate email`() {
        val request =
                RegisterRequest(
                        email = "register-duplicate@example.com",
                        password = "password",
                        "Test User"
                )
        authService.register(request)
        assertThrows(EmailAlreadyRegisteredException::class.java) { authService.register(request) }
    }

    @Test
    fun `login with valid credentials returns a token`() {
        val email = "login-valid@example.com"
        val request = LoginRequest(email = email, password = "password")
        authService.register(
                RegisterRequest(email = email, password = "password", fullName = "Test User")
        )
        // Test implementation here
        assertDoesNotThrow {
            val response = authService.login(request)
            assertTrue(response.token.isNotEmpty(), "Token should not be empty")
        }
    }

    @Test
    fun `login with invalid credentials throws exception`() {
        val email = "login-invalid@example.com"

        authService.register(
                RegisterRequest(email = email, password = "password", fullName = "Test User")
        )
        // Test implementation here
        assertThrows(InvalidCredentialsException::class.java) {
            authService.login(LoginRequest(email = email, password = "wrongpassword"))
        }
    }
}
