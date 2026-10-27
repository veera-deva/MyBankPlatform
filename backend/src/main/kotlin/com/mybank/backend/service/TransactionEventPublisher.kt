package com.mybank.backend.service


// Port: the ledger announces "this entry was posted" without knowing it is delivered.
interface TransactionEventPublisher {
    fun publish(entry: com.mybank.backend.domain.LedgerEntry)
}
