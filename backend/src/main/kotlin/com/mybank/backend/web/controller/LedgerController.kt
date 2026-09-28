package com.mybank.backend.web.controller

import com.mybank.backend.web.dto.AmountRequest
import com.mybank.backend.web.dto.LedgerEntryResponse
import com.mybank.backend.web.dto.toResponse
import com.mybank.backend.web.dto.BalanceResponse
import com.mybank.backend.web.dto.PageResponse
import com.mybank.backend.service.AccountTransactionService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@RestController
@RequestMapping("/accounts/{accountId}")
class LedgerController(private val transactionService: AccountTransactionService) {

    @PostMapping("/deposit")
    fun deposit(
            @AuthenticationPrincipal customerIdd: UUID,
            @PathVariable accountId: UUID,
            @Valid @RequestBody request: AmountRequest
    ): ResponseEntity<LedgerEntryResponse> {
        val ledgerEntry = transactionService.deposit(customerIdd, accountId, request.amount)
        return ResponseEntity.status(201).body(ledgerEntry.toResponse())
    }

    @PostMapping("/withdraw")
    fun withdraw(
            @AuthenticationPrincipal customerIdd: UUID,
            @PathVariable accountId: UUID,
            @Valid @RequestBody request: AmountRequest
    ): ResponseEntity<LedgerEntryResponse> {
        val ledgerEntry = transactionService.withdraw(customerIdd, accountId, request.amount)
        return ResponseEntity.status(201).body(ledgerEntry.toResponse())
    }

    // Endpoint to get the balance of an account
    @GetMapping("/balance")
    fun getBalance(@AuthenticationPrincipal customerId: UUID, @PathVariable accountId: UUID): ResponseEntity<BalanceResponse> {
        val balance = transactionService.currentBalance(customerId = customerId, accountId = accountId)
        return ResponseEntity.ok(BalanceResponse(accountId, balance))
    }
    
    @GetMapping("/transactions")
    fun history(
        @AuthenticationPrincipal customerId: UUID, 
        @PathVariable("accountId") accountId: UUID,
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "20") size: Int
        ): ResponseEntity<PageResponse<LedgerEntryResponse>> {
        val ledgerEntries = transactionService.history(customerId, accountId, page, size)
        return ResponseEntity.ok(PageResponse.from(ledgerEntries) { it.toResponse() })
    }
}
