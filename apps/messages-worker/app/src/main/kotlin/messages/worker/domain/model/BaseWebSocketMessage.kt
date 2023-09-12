package messages.worker.domain.model

import kotlinx.serialization.Serializable


@Serializable
enum class WebSocketMessageType {
    NEW_MESSAGE, MEMBER_CONNECTED, MEMBER_DISCONNECTED
}

@Serializable
abstract class BaseWebSocketMessage(
    val message: WebSocketMessageType
)
