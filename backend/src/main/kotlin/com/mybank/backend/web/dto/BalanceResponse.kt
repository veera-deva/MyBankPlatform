package com.mybank.backend.web.dto

import java.math.BigDecimal
import java.util.UUID
data class BalanceResponse(val accountId:UUID, val balance: BigDecimal)
