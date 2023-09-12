package messages.worker.infrastructure.amqp.kafka

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("MEMBER_DISCONNECTED")
class MemberDisconnected(val memberId: Long, val userName: String, override val messageType: BaseMessageType = BaseMessageType.MEMBER_DISCONNECTED) : BaseMessage()