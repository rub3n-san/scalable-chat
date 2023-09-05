package system.test.integration.model

import kotlinx.serialization.Serializable

@Serializable
data class PostMessageDto(val content: String)