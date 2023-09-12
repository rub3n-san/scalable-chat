package ws.server.infrastructure.amqp.kafka

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
@SerialName("NEW_MESSAGE")
data class NewMessage(val documentId: String, override val messageType: BaseMessageType = BaseMessageType.NEW_MESSAGE) : BaseMessage()