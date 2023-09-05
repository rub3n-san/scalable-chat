package ws.server.api.model

import kotlinx.serialization.Serializable

@Serializable
data class PostMessageDto(val content: String)
