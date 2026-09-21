package com.mybank.backend.web.controller

import com.mybank.backend.service.AccountService
import com.mybank.backend.web.dto.AccountResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


@RestController
@RequestMapping("/accounts")
class AccountController(private val accountService: AccountService) {

    @PostMapping
    fun createAccount(@AuthenticationPrincipal customerId: UUID): ResponseEntity<AccountResponse> {
        val response = accountService.createAccount(customerId)
        return ResponseEntity.status(201).body(response)
    }

}
