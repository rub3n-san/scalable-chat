package ws.server.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ExceptionResponse(
    val message: String,
    val code: Int
)