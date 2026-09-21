package com.mybank.backend.web

import com.mybank.backend.service.AccountAlreadyExistException
import com.mybank.backend.service.EmailAlreadyRegisteredException
import com.mybank.backend.service.InsufficientFundsException
import com.mybank.backend.service.InvalidCredentialsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {


    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(
            HttpStatus.UNAUTHORIZED
        ).body(mapOf("error" to ex.message.orEmpty()))

    @ExceptionHandler(EmailAlreadyRegisteredException::class)
    fun handelEmailAlreadyRegistered(ex: EmailAlreadyRegisteredException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(
            HttpStatus.CONFLICT
        ).body(mapOf("error" to ex.message.orEmpty()))


    @ExceptionHandler(InsufficientFundsException::class)
    fun handleInEfficientFundException(ex: InsufficientFundsException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to ex.message.orEmpty()))

    @ExceptionHandler(AccountAlreadyExistException::class)
    fun handleAccountAlreadyExists(ex: AccountAlreadyExistException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to ex.message.orEmpty()))

}