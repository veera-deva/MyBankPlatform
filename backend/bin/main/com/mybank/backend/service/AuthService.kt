package com.mybank.backend.service

import com.mybank.backend.domain.Customer
import com.mybank.backend.repository.CustomerRepository
import com.mybank.backend.security.JwtService
import com.mybank.backend.web.dto.AuthResponse
import com.mybank.backend.web.dto.LoginRequest
import com.mybank.backend.web.dto.RegisterRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val customerRepository: CustomerRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @org.springframework.transaction.annotation.Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (customerRepository.findByEmail(request.email) != null) {
            throw EmailAlreadyRegisteredException("Email already registered: ${request.email}")
        }
        val customer = customerRepository.save(
            Customer(
                email = request.email,
                passwordHash = passwordEncoder.encode(request.password),
                fullName = request.fullName
            )
        )
        val token = jwtService.generateToken(customer.id!!, customer.email)
        return AuthResponse(token)
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val customer = customerRepository.findByEmail(request.email)
            ?: throw InvalidCredentialsException("Invalid email or password")
        if (!passwordEncoder.matches(request.password, customer.passwordHash)) {
            throw InvalidCredentialsException("Invalid email or password")
        }

        val token = jwtService.generateToken(customer.id!!, customer.email)
        return AuthResponse(token)
    }


}