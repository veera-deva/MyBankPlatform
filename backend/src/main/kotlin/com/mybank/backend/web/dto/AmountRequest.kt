package com.mybank.backend.web.dto

import java.math.BigDecimal
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
data class AmountRequest(
    
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @field:Digits(integer = 17, fraction = 2, message = "Amount must have at most 10 digits and 2 decimal places")
    val amount: BigDecimal
)

