package messages.worker.infrastructure.amqp.kafka

import kotlinx.serialization.Serializable

@Serializable
data class Chat(val documentId: String)
