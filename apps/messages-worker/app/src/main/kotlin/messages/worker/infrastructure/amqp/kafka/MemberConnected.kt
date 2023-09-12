package messages.worker.infrastructure.amqp.kafka

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("MEMBER_CONNECTED")
class MemberConnected(val memberId: Long, val userName: String,
                      override val messageType: BaseMessageType = BaseMessageType.MEMBER_CONNECTED) : BaseMessage()
