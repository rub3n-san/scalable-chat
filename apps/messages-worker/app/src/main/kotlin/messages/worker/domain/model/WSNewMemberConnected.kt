package messages.worker.domain.model


import kotlinx.serialization.Serializable

@Serializable
data class WSNewMemberConnected(val userName: String) : BaseWebSocketMessage(WebSocketMessageType.MEMBER_CONNECTED)