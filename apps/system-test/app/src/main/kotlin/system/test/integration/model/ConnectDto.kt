package system.test.integration.model

import kotlinx.serialization.Serializable

@Serializable
data class ConnectDto(val webSocket: WebSocketDto, val chat: ChatDto?)

@Serializable
data class ChatDto(val messages: List<MessageDto>)