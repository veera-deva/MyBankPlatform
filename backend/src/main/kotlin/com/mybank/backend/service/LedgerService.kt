package com.mybank.backend.service

import com.mybank.backend.domain.LedgerEntry
import com.mybank.backend.domain.TransactionType
import com.mybank.backend.repository.AccountRepository
import com.mybank.backend.repository.LedgerEntryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
class LedgerService(
    private val accountRepository: AccountRepository,
    private val ledgerEntryRepository: LedgerEntryRepository
) {

    @Transactional
    fun deposit(accountId: UUID, amount: BigDecimal): LedgerEntry {
        require(amount > BigDecimal.ZERO) { "Deposit amount must be positie" }

        val account =
            accountRepository.lockById(accountId) ?: throw NoSuchElementException("Account not found:$accountId")
        val newBalance = currentBalance(accountId) + amount

        return ledgerEntryRepository.save(
            LedgerEntry(
                account = account,
                type = TransactionType.DEPOSIT,
                amount = amount,
                balanceAfter = newBalance
            )
        )

    }

    @Transactional
    fun withdraw(accountId: UUID, amount: BigDecimal): LedgerEntry {
        require(amount > BigDecimal.ZERO) { "Withdrawal amount must be positive" }
        val account =
            accountRepository.lockById(accountId) ?: throw NoSuchElementException("Account not found: $accountId")

        val balanceBeforeWithdrawal = currentBalance(accountId)
        if (amount > balanceBeforeWithdrawal) {
            throw InsufficientFundsException(" Cannot withdraw $amount from account $accountId; current balance is $balanceBeforeWithdrawal")
        }
        return ledgerEntryRepository.save(
            LedgerEntry(
                account = account,
                type = TransactionType.WITHDRAWAL,
                amount = amount,
                balanceAfter = balanceBeforeWithdrawal - amount
            )
        )
    }

    @Transactional(readOnly = true)
    fun currentBalance(accountId: UUID): BigDecimal =
        ledgerEntryRepository.findTopByAccountIdOrderByCreatedAtDesc(accountId)?.balanceAfter ?: BigDecimal.ZERO


}