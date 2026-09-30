package com.mybank.backend.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.mybank.backend.TEST_JWT_SECRET
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = ["app.jwt.secret=$TEST_JWT_SECRET"])
class LedgerApiTest {

    companion object {
        // Testcontainers setup for PostgreSQL

        @Container
        @ServiceConnection
        val postgres = org.testcontainers.containers.PostgreSQLContainer("postgres:16")
    }

    @Autowired lateinit var mockMvc: org.springframework.test.web.servlet.MockMvc

    private fun uniqueEmail() = "t6-${java.util.UUID.randomUUID()}@example.com"

    private fun registeAndGetToken(email: String = uniqueEmail()): String {
        val body = mapOf("email" to email, "password" to "password123", "fullName" to "Test User")
        val result =
                mockMvc.perform(
                                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                        .post("/auth/register")
                                        .contentType(
                                                org.springframework.http.MediaType.APPLICATION_JSON
                                        )
                                        .content(ObjectMapper().writeValueAsString(body))
                        )
                        .andExpect(
                                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                        .status()
                                        .isCreated
                        )
                        .andReturn()
        return ObjectMapper().readTree(result.response.contentAsString).get("token").asText()
    }

    private fun createAccount(token: String): String {
        val body = mapOf("accountType" to "SAVINGS", "initialDeposit" to 1000.0)
        val result =
                mockMvc.perform(
                                post("/accounts")
                                        .header("Authorization", "Bearer $token")
                                        .contentType(
                                                org.springframework.http.MediaType.APPLICATION_JSON
                                        )
                                        .content(ObjectMapper().writeValueAsString(body))
                        )
                        .andExpect(
                                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                        .status()
                                        .isCreated
                        )
                        .andReturn()
        return ObjectMapper().readTree(result.response.contentAsString).get("id").asText()
    }

    private fun deposit(token: String, accountId: String, amount: Double) {
        val body = mapOf("amount" to amount)
        mockMvc.perform(
                        post("/accounts/$accountId/deposit")
                                .header("Authorization", "Bearer $token")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content(ObjectMapper().writeValueAsString(body))
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers.status()
                                .isCreated
                )
    }

    @Test
    fun `deposit success and returns new balance`() {
        val token = registeAndGetToken()
        val accountId = createAccount(token)
        mockMvc.perform(
                        post("/accounts/$accountId/deposit")
                                .header("Authorization", "Bearer $token")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content(
                                        ObjectMapper().writeValueAsString(mapOf("amount" to 500.0))
                                )
                )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.balanceAfter").value(500.0))
    }

    @Test
    fun `withdraw success and returns new balance`() {
        val token = registeAndGetToken()
        val accountId = createAccount(token)
        deposit(token, accountId, 500.0) // Deposit some amount first

        mockMvc.perform(
                        post("/accounts/$accountId/withdraw")
                                .header("Authorization", "Bearer $token")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content(
                                        ObjectMapper().writeValueAsString(mapOf("amount" to 200.0))
                                )
                )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.balanceAfter").value(300.0)) // 500 - 200 = 3001
    }

    @Test
    fun `withdraw fails with insufficient funds`() {
        val token = registeAndGetToken()
        val accountId = createAccount(token)
        deposit(token, accountId, 1000.0) // Deposit some amount first
        mockMvc.perform(
                        post("/accounts/$accountId/withdraw")
                                .header("Authorization", "Bearer $token")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content(
                                        ObjectMapper()
                                                .writeValueAsString(
                                                        mapOf("amount" to 1500.0)
                                                ) // Attempt to withdraw more than the balance
                                )
                )
                .andExpect(status().isUnprocessableEntity)
                .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `depositing more than two decimal places fails with 400`() {
        val token = registeAndGetToken()
        val accountId = createAccount(token)
        mockMvc.perform(
                        post("/accounts/$accountId/deposit")
                                .header("Authorization", "Bearer $token")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content(
                                        ObjectMapper()
                                                .writeValueAsString(
                                                        mapOf("amount" to 100.123)
                                                ) // More than two decimal places
                                )
                )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.fields.amount").exists())
    }

    @Test
    fun `balance reflects deposits and withdrawals`() {
        val token = registeAndGetToken()
        val accountId = createAccount(token)

        // Initial deposit
        deposit(token, accountId, 1000.0)

        // Withdraw some amount
        mockMvc.perform(
                        post("/accounts/$accountId/withdraw")
                                .header("Authorization", "Bearer $token")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content(
                                        ObjectMapper().writeValueAsString(mapOf("amount" to 300.0))
                                )
                )
                .andExpect(status().isCreated)

        // Check balance
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                        "/accounts/$accountId/balance"
                                )
                                .header("Authorization", "Bearer $token")
                )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.balance").value(700.0)) // 1000 - 300 = 700
    }

    @Test
    fun `history caps the page size even when a larger size is requested`() {
        val token = registeAndGetToken()
        val accountId = createAccount(token)

        // Make 30 deposits
        for (i in 1..30) {
            deposit(token, accountId, 10.0)
        }

        // Request history with a size larger than the cap (e.g., 50)
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                        "/accounts/$accountId/transactions"
                                )
                                .header("Authorization", "Bearer $token")
                                .param("page", "0")
                                .param("size", "10000") // Requesting more than the cap
                )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.size").value(100)) // Should be capped at 20
    }

    @Test
    fun `accessing another customer's account returns 404, not 403`() {
        val token1 = registeAndGetToken()
        val accountId1 = createAccount(token1)
        val token2 = registeAndGetToken() // Different customer

        // Try to access account 1 with token 2
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                        "/accounts/$accountId1/balance"
                                )
                                .header("Authorization", "Bearer $token2")
                )
                .andExpect(status().isNotFound) // Should return 404, not 403
                .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `an unknown account id returns 404`() {
        val token = registeAndGetToken()
        val unknownAccountId = java.util.UUID.randomUUID().toString()

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                        "/accounts/$unknownAccountId/balance"
                                )
                                .header("Authorization", "Bearer $token")
                )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `a request without a JWT returns 401 with a WWW-Authenticate header`() {
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                "/accounts/${java.util.UUID.randomUUID()}/balance"
                        )
                )
                .andExpect(status().isUnauthorized)
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                                .string("WWW-Authenticate", "Bearer")
                )
                // .andExpect(jsonPath("$.error").value("Authentication required"))
    }
    
    @Test
    fun `a malformed JWT returns 401`() {
      mockMvc.perform(
          org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
              "/accounts/${java.util.UUID.randomUUID()}/balance"
          )
              .header("Authorization", "Bearer malformed.jwt.token")
      )
          .andExpect(status().isUnauthorized)
         
    }
        
}
