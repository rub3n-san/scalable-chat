package ws.server.domain.model


import kotlinx.serialization.Serializable
import org.bson.Document
import ws.server.config.serialize.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class ChatContent(
    val id: String,
    val content: String,
    val userName: String,
    val channelName: String,
    @Serializable(with = LocalDateTimeSerializer::class) var createdAt: LocalDateTime
) {
    companion object {
        fun fromMongoDbDocument(d: Document): ChatContent =
            ChatContent(
                id = d.getObjectId("_id").toString(),
                content = d.getString("content"),
                channelName = d.getString("channelName"),
                userName = d.getString("userName"),
                createdAt = LocalDateTime.parse(d.getString("createdAt"))
            )

        fun toMongoDbDocument(
            content: String,
            channelName: String,
            userName: String,
            createdAt: LocalDateTime
        ): Document =
            Document(
                mapOf(
                    "content" to content,
                    "userName" to userName,
                    "channelName" to channelName,
                    "createdAt" to createdAt.toString()
                )
            )
    }
}
