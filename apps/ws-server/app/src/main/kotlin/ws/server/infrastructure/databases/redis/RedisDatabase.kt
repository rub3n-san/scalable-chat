package ws.server.infrastructure.databases.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import ws.server.domain.model.MessageWorker
import java.util.*

class ReddisDatabase(properties: Properties) {
    val host = properties.getProperty("database.redis.host", "localhost")
    val port = properties.getProperty("database.redis.port", "6379").toInt()
    val user = properties.getProperty("database.redis.user", "default")
    val password = properties.getProperty("database.redis.password", "XXXXX")
    val databaseNumber = properties.getProperty("database.redis.database", "1").toInt()

    fun getConnection(): RedisCommands<String, String> {
        val redisURI = RedisURI.Builder.redis(host, port)
            .withAuthentication(user, password.toCharArray())
            .withDatabase(databaseNumber).build()

        val redisClient: RedisClient = RedisClient.create(redisURI)
        val connection: StatefulRedisConnection<String, String> = redisClient.connect()
        return connection.sync()
    }

}

class RedisStoreImpl(val reddisDatabase: ReddisDatabase) : MessageWorkersStore {

    val MAX_USERS_PER_MESSAGE_WORKER = 150L // user properties
    override fun findLowestRankWorker(): MessageWorker {

        val messageWorkers =
            reddisDatabase.getConnection().zrange("message_workers", 0, MAX_USERS_PER_MESSAGE_WORKER)

        if (messageWorkers == null || messageWorkers.isEmpty()) {
            throw Throwable("No message workers on duty!")
        }

        val idlestMessageWorker = messageWorkers.first()
        println("Idlest message worker is $idlestMessageWorker")
        return MessageWorker(idlestMessageWorker)
    }
}