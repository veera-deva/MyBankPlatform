package com.mybank.backend.repository

import com.mybank.backend.domain.LedgerEntry
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LedgerEntryRepository : JpaRepository<LedgerEntry, UUID> {

    fun findTopByAccountIdOrderByCreatedAtDesc(accountId: UUID): LedgerEntry?
}
