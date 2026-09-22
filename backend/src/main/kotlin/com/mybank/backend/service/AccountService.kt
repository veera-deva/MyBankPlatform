package com.mybank.backend.service

import com.mybank.backend.domain.Account
import com.mybank.backend.repository.AccountRepository
import com.mybank.backend.repository.CustomerRepository
import com.mybank.backend.web.dto.AccountResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.random.Random


@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val customerRepository: CustomerRepository
) {

    @Transactional
    fun createAccount(customerId: UUID): AccountResponse {

        if (accountRepository.findAccountByCustomerId(customerId) != null) {
            throw AccountAlreadyExistException("Customer already has an account: $customerId")
        }
        val customer = customerRepository.findById(customerId)
            .orElseThrow { NoSuchElementException("Customer not found: $customerId") }

        val account =
            accountRepository.save(Account(customer = customer, accountNumber = generateUniqueAccountNumber()))

        return AccountResponse(
            id = account.id!!,
            accountNumber = account.accountNumber,
            status = account.status,
            createdAt = account.createdAt
        )

    }


    private fun generateUniqueAccountNumber(): String {
        repeat(5) {
            val candidate = (1..10).map { Random.nextInt(0, 10) }.joinToString("")
            if (accountRepository.findByAccountNumber(candidate) == null) {
                return candidate
            }
        }
        throw IllegalStateException("Failed to generate a unique account number after 5 attempts")
    }


}