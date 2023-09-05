package ws.server.infrastructure.databases.mongodb

import messages.worker.domain.model.ChatContent


interface ChatContentStore {
    fun fetchContent(documentIds: List<String>): List<ChatContent>
}