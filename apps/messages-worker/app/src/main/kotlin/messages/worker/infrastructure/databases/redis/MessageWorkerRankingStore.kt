package messages.worker.infrastructure.databases.redis

interface MessageWorkerRankingStore {
    fun registerSelf(): Long
    fun increaseRanking(messageWorker: String)
    fun decreaseRanking(messageWorker: String)
    fun removeSelf(messageWorker: String)
}