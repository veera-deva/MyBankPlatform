package com.mybank.backend.web.dto

import com.mybank.backend.domain.AccountStatus
import java.time.OffsetDateTime
import java.util.UUID

data class AccountResponse(
    val id: UUID,
    val accountNumber: String,
    val status: AccountStatus,
    val createdAt: OffsetDateTime
)
