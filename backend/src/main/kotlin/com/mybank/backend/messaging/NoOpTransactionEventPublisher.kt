package com.mybank.backend.messaging

import com.mybank.backend.domain.LedgerEntry
import com.mybank.backend.service.TransactionEventPublisher
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
        name = ["app.events.kafka.enabled"],
        havingValue = "false")
class NoOpTransactionEventPublisher : TransactionEventPublisher {

    override fun publish(entry: LedgerEntry) = Unit
}
