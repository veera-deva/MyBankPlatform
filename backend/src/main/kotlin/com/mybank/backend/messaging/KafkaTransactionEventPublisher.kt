package com.mybank.backend.messaging

import com.mybank.backend.domain.LedgerEntry
import com.mybank.backend.service.TransactionEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["app.events.kafka.enabled"], havingValue = "true")
class KafkaTransactionEventPublisher(
        private val kafkaTemplate: KafkaTemplate<String, ByteArray>,
        @Value("\${app.events.kafka.topics.transaction-posted}") private val topic: String
) : TransactionEventPublisher {

    private val log = LoggerFactory.getLogger(javaClass)
    override fun publish(entry: LedgerEntry) {
        val event = TransactionPostedMapper.toEvent(entry)
        kafkaTemplate.send(topic, event.accountId, event.toByteArray()).whenComplete { result, ex ->
            if (ex != null) {
                log.error(
                        "Failed to publish TransactionPosted EventId={} ledgerEntryId={}",
                        event.eventId,
                        event.ledgerEntryId,
                        ex
                )
            } else {
                log.info(
                        "Published TransactionPosted eventId={} key={} partition={} offset={}",
                        event.eventId,
                        event.accountId,
                        result.recordMetadata.partition(),
                        result.recordMetadata.offset()
                )
            }
        }
    }
}
