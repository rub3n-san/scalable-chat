package messages.worker.domain.model


import kotlinx.serialization.Serializable

@Serializable
data class WSMemberDisconnected( val userName: String) : BaseWebSocketMessage(WebSocketMessageType.MEMBER_DISCONNECTED)