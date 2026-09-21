package com.mybank.backend.repository

import com.mybank.backend.domain.Customer
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CustomerRepository : JpaRepository<Customer, UUID> {
    fun findByEmail(email: String): Customer?
}
