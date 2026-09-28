package com.mybank.backend.repository

import com.mybank.backend.domain.LedgerEntry
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository

interface LedgerEntryRepository : JpaRepository<LedgerEntry, UUID> {

    fun findTopByAccountIdOrderByCreatedAtDesc(accountId: UUID): LedgerEntry?
    fun findByAccountId(
            accountId: UUID,
            pageable: org.springframework.data.domain.Pageable
    ): Page<LedgerEntry>
}
