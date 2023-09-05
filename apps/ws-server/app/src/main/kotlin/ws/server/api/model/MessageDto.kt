package ws.server.api.model


import kotlinx.serialization.Serializable
import ws.server.config.serialize.LocalDateTimeSerializer
import ws.server.domain.model.Message
import java.time.LocalDateTime

@Serializable
data class MessageDto(
    val content: String,
    @Serializable(with = LocalDateTimeSerializer::class) val createdAt: LocalDateTime, val userName: String
) {
    companion object {
        fun toApi(m: Message) = MessageDto(m.content, m.createdAt, m.userName)
    }
}
