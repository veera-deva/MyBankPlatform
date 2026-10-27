package com.mybank.backend.messaging

import com.google.protobuf.Timestamp
import com.mybank.backend.domain.TransactionType
import com.mybank.backend.domain.TransactionType.DEPOSIT
import com.mybank.backend.domain.TransactionType.WITHDRAWAL
import com.mybank.contracts.ledger.v1.TransactionPosted
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import com.mybank.contracts.ledger.v1.TransactionType as TransactionTypeProto

object TransactionPostedMapper {
        fun toEvent(entry: com.mybank.backend.domain.LedgerEntry): TransactionPosted =
                TransactionPosted.newBuilder()
                        .setEventId(UUID.randomUUID().toString())
                        .setLedgerEntryId(entry.id!!.toString())
                        .setAccountId(entry.account.id!!.toString())
                        .setCustomerId(entry.account.customer.id!!.toString())
                        .setType(toProto(entry.type))
                        .setAmount(money(entry.amount))
                        .setBalanceAfter(money(entry.balanceAfter))
                        .setOccurredAt(
                                Timestamp.newBuilder()
                                        .setSeconds(entry.createdAt.toEpochSecond())
                                        .setNanos(entry.createdAt.nano)
                                        .build()
                        )
                        .build()
}

private fun toProto(type: TransactionType): TransactionTypeProto =
        when (type) {
                DEPOSIT -> TransactionTypeProto.TRANSACTION_TYPE_DEPOSIT
                WITHDRAWAL -> TransactionTypeProto.TRANSACTION_TYPE_WITHDRAWAL
        }

// Canonical two decimal string ("100" -> "100.00"). UNNECESSARY makes a value with more than two
// decimals fail loudly instead of being silently rounded (NFR-3)
private fun money(value: BigDecimal): String =
        value.setScale(2, RoundingMode.UNNECESSARY).toPlainString()
