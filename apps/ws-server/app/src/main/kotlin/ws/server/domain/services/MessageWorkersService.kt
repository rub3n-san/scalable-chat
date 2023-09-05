package ws.server.domain.services

import ws.server.domain.model.MessageWorker
import ws.server.infrastructure.databases.redis.MessageWorkersStore

class MessageWorkersService(val messageWorkerStore: MessageWorkersStore) {
    fun findSuitableMessageWorker(): MessageWorker = messageWorkerStore.findLowestRankWorker()

}