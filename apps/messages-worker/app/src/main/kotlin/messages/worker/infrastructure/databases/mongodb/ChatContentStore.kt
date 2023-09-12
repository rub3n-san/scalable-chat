package ws.server.infrastructure.databases.mongodb

import messages.worker.domain.model.WSChatContent


interface ChatContentStore {
    fun fetchContent(documentIds: List<String>): List<WSChatContent>
}