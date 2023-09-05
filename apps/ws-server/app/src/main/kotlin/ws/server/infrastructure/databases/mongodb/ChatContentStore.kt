package ws.server.infrastructure.databases.mongodb

import ws.server.domain.model.ChatContent
import java.time.LocalDateTime

interface ChatContentStore {
    fun fetchContent(documentIds: List<String>): List<ChatContent>
    fun saveContent(content: String, channelName: String, userName: String, createdAt: LocalDateTime): String
}