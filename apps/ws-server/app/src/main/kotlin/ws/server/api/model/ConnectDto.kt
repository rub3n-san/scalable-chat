package ws.server.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ConnectDto(val webSocket: WebSocketDto, val chat: ChatDto?, val activeMembers: List<UserDto>)

@Serializable
data class ChatDto(val messages: List<MessageDto>)

@Serializable
data class UserDto(val name: String)