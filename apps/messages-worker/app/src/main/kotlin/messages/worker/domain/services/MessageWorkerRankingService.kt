package messages.worker.domain.services

import messages.worker.infrastructure.databases.redis.MessageWorkerRankingStore

class MessageWorkerRankingService(
    private val messageWorkerRankingStore: MessageWorkerRankingStore,
    private val selfUrl: String
) {
    val selfNumber: Long = messageWorkerRankingStore.registerSelf()

    fun increase() = messageWorkerRankingStore.increaseRanking(selfUrl)
    fun decrease() = messageWorkerRankingStore.decreaseRanking(selfUrl)
    fun removeSelf() = messageWorkerRankingStore.removeSelf(selfUrl)

}