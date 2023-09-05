package ws.server.infrastructure.databases.redis

import ws.server.domain.model.MessageWorker

interface MessageWorkersStore {
    fun findLowestRankWorker(): MessageWorker
}