package com.mybank.backend.web.dto

import com.mybank.backend.domain.TransactionType
import com.mybank.backend.domain.LedgerEntry
import java.time.OffsetDateTime
import java.math.BigDecimal 
import java.util.UUID

class LedgerEntryResponse(val id: UUID, val type: TransactionType, val amount: BigDecimal, val balanceAfter: BigDecimal, val createdAt: OffsetDateTime)

fun LedgerEntry.toResponse()= LedgerEntryResponse(
    id = this.id!!,
    type = this.type,
    amount = this.amount,
    balanceAfter = this.balanceAfter,
    createdAt = this.createdAt
)