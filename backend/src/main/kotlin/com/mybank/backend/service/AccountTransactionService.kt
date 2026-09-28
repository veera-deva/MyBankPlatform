package com.mybank.backend.service
import com.mybank.backend.domain.LedgerEntry
import com.mybank.backend.repository.AccountRepository
import com.mybank.backend.service.AccountNotFoundException
import com.mybank.backend.service.LedgerService
import java.math.BigDecimal
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@Service
class AccountTransactionService(
        private val accountRepository: AccountRepository,
        private val ledgerEntryRepository: com.mybank.backend.repository.LedgerEntryRepository,
        private val ledgerService: LedgerService
) {

    fun deposit(customerId: UUID, accountId: UUID, amount: BigDecimal): LedgerEntry {
        ownedAccount(customerId, accountId)

        return ledgerService.deposit(accountId, amount)
    }

    fun withdraw(customerId: UUID, accountId: UUID, amount: BigDecimal): LedgerEntry {
        ownedAccount(customerId, accountId)

        return ledgerService.withdraw(accountId, amount)
    }

    @Transactional(readOnly = true)
    fun currentBalance(customerId: UUID, accountId: UUID): BigDecimal {
        ownedAccount(customerId, accountId)

        return ledgerService.currentBalance(accountId)
    }
    
    @Transactional(readOnly = true)
    fun history(customerId: UUID, accountId: UUID, page: Int, size: Int): Page<LedgerEntry> {
        ownedAccount(customerId, accountId)
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, MAX_PAGE_SIZE),Sort.by(Sort.Direction.DESC, "createdAt"))
        
        return ledgerEntryRepository.findByAccountId(accountId, pageable)
    }

    fun ownedAccount(customerId: UUID, accountId: UUID) {
        accountRepository.findByIdAndCustomerId(accountId, customerId)
                ?: throw AccountNotFoundException("Account not found")
    }
    companion object {
        const val MAX_PAGE_SIZE = 100
    }
}
