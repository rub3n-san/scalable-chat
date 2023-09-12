package ws.server.infrastructure.amqp.kafka

import kotlinx.serialization.Serializable


@Serializable
abstract class BaseMessage{
    abstract val messageType: BaseMessageType
}
@Serializable
enum class BaseMessageType {
    NEW_MESSAGE, MEMBER_CONNECTED, MEMBER_DISCONNECTED
}
