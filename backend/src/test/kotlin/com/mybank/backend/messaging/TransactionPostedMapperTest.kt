package com.mybank.backend.messaging

import com.mybank.backend.domain.Account
import com.mybank.backend.domain.Customer
import com.mybank.backend.domain.LedgerEntry
import com.mybank.backend.domain.TransactionType
import com.mybank.contracts.ledger.v1.TransactionPosted
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import com.mybank.contracts.ledger.v1.TransactionType as TransactionTypeProto

class TransactionPostedMapperTest {
        private fun ledgerEntry(
                type: TransactionType,
                amount: String,
                balanceAfter: String
        ): LedgerEntry {

                val customerEx =
                        Customer(
                                        email = "mapper@example.com",
                                        passwordHash = "hash",
                                        fullName = "Mapper Test"
                                )
                                .apply { id = UUID.randomUUID() }
                val account =
                        Account(customer = customerEx, accountNumber = "1234566890").apply {
                                id = UUID.randomUUID()
                        }

                return LedgerEntry(
                                account,
                                type,
                                amount = BigDecimal(amount),
                                balanceAfter = BigDecimal(balanceAfter)
                        )
                        .apply { id = UUID.randomUUID() }
        }

        private fun roundTrip(entry: LedgerEntry): TransactionPosted =
                TransactionPosted.parseFrom(TransactionPostedMapper.toEvent(entry).toByteArray())

        @Test
        fun `maps every field and survives a serialize-parse round trip`() {
                val entry = ledgerEntry(TransactionType.DEPOSIT, "100.00", "100.00")
                val parsed = roundTrip(entry)

                assertEquals(entry.id.toString(), parsed.ledgerEntryId)
                assertEquals(entry.account.id.toString(), parsed.accountId)
                assertEquals(entry.account.customer.id.toString(), parsed.customerId)
                assertEquals(TransactionTypeProto.TRANSACTION_TYPE_DEPOSIT, parsed.type)
                assertEquals("100.00", parsed.amount)
                assertEquals("100.00", parsed.balanceAfter)
                assertEquals(entry.createdAt.toEpochSecond(), parsed.occurredAt.seconds)
                assertEquals(entry.createdAt.nano, parsed.occurredAt.nanos)
        }

        @Test
        fun `withdrawal maps to the withdrawal enum`() {
                val parsed = roundTrip(ledgerEntry(TransactionType.WITHDRAWAL, "30.00", "70.00"))

                assertEquals(TransactionTypeProto.TRANSACTION_TYPE_WITHDRAWAL, parsed.type)
                assertEquals("70.00", parsed.balanceAfter)
        }

        @Test
        fun `money is always encoded with exactly two decimals`() {
                val parsed = roundTrip(ledgerEntry(TransactionType.DEPOSIT, "100", "100"))

                assertEquals("100.00", parsed.amount)
                assertEquals("100.00", parsed.balanceAfter)
        }
        @Test
        fun `an amount with more than two decimals fails instead of being rounded`() {
                assertFailsWith<ArithmeticException> {
                        TransactionPostedMapper.toEvent(
                                ledgerEntry(TransactionType.DEPOSIT, "0.001", "0.001")
                        )
                }
        }

        @Test
        fun `each event gets a fresh eventId even for the same ledger entry`() {
                val entry = ledgerEntry(TransactionType.DEPOSIT, "10.00", "10.00")

                assertNotEquals(
                        TransactionPostedMapper.toEvent(entry).eventId,
                        TransactionPostedMapper.toEvent(entry).eventId
                )
        }
}
