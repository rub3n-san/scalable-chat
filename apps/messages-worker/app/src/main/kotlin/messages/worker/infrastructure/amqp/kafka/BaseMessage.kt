package messages.worker.infrastructure.amqp.kafka

import kotlinx.serialization.Serializable


@Serializable
abstract class BaseMessage{
    abstract val messageType: BaseMessageType
}

class ErrorMessage(override val messageType: BaseMessageType = BaseMessageType.ERROR) : BaseMessage()
@Serializable
enum class BaseMessageType {
    NEW_MESSAGE, MEMBER_CONNECTED, MEMBER_DISCONNECTED, ERROR
}
