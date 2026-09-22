package com.mybank.backend.repository

import com.mybank.backend.domain.Account
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.UUID


interface AccountRepository : JpaRepository<Account, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id=:id")
    fun lockById(id: UUID): Account?

    fun findAccountByCustomerId(customerId: UUID): Account?

    fun findByAccountNumber(accountNumber: String): Account?
}
