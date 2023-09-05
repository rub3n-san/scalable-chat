package system.test.integration.model


import kotlinx.serialization.Serializable
import ws.server.config.serialize.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class MessageDto(
    val content: String,
    @Serializable(with = LocalDateTimeSerializer::class) val createdAt: LocalDateTime, val userName: String
)