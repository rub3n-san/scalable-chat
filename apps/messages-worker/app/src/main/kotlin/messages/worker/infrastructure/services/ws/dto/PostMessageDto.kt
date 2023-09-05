package messages.worker.infrastructure.services.ws.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostMessageDto(val content: String)