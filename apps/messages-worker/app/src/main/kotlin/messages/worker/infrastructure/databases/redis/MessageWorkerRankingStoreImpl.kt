package messages.worker.infrastructure.databases.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import java.util.*

class RedisDatabase(properties: Properties) {
    val host = properties.getProperty("database.redis.host", "localhost")
    val port = properties.getProperty("database.redis.port", "6379").toInt()
    val user = properties.getProperty("database.redis.user", "default")
    val password = properties.getProperty("database.redis.password", "XXXXX")
    val databaseNumber = properties.getProperty("database.redis.database", "1").toInt()

    val redisURI: RedisURI = RedisURI.Builder.redis(host, port)
        .withAuthentication(user, password.toCharArray())
        .withDatabase(databaseNumber).build()

    fun getConnection(): RedisCommands<String, String> {
        val redisClient: RedisClient = RedisClient.create(redisURI)
        val connection: StatefulRedisConnection<String, String> = redisClient.connect()
        return connection.sync()
    }

    fun <T> executeInRedisTransaction(block: (RedisCommands<String, String>) -> T): T {
        val redisClient: RedisClient = RedisClient.create(redisURI)
        val connection: StatefulRedisConnection<String, String> = redisClient.connect()
        val syncCommands: RedisCommands<String, String> = connection.sync()

        return try {
            // Start a transaction
            syncCommands.multi()

            // Execute the user-defined block
            val result = block(syncCommands)

            // Execute the transaction
            syncCommands.exec()

            result
        } catch (e: Exception) {
            // Handle exceptions and potentially rollback the transaction
            syncCommands.discard()
            throw e
        } finally {
            // Release the connection back to the pool
            connection.close()
            redisClient.shutdown()
        }
    }

    fun <T> executeRedisCommand(
        block: (RedisCommands<String, String>) -> T
    ): T {
        val redisClient: RedisClient = RedisClient.create(redisURI)
        val connection: StatefulRedisConnection<String, String> = redisClient.connect()
        val asyncCommands: RedisCommands<String, String> = connection.sync()

        return try {
            val result = block(asyncCommands)
            result
        } catch (e: Exception) {
            throw e
        } finally {
            connection.close()
            redisClient.shutdown()
        }
    }


}

class MessageWorkerRankingStoreImpl(private val redisDatabase: RedisDatabase, val selfUrl: String) :
    MessageWorkerRankingStore {
    override fun registerSelf(): Long {
        redisDatabase.getConnection().zadd("message_workers", 0.0, selfUrl)
        val number = redisDatabase.getConnection().zcard("message_workers")
        println("Registered server $selfUrl on redis with $number.")
        return number
        /* FIXME: This should be transactional instead of the above
        return redisDatabase.executeInRedisTransaction { c ->
            c.zadd("message_workers", 0.0, selfUrl)
            val number = c.zcard("message_workers")
            println("Registered server $selfUrl on redis with $number.")
            number
        }
         */
    }

    override fun decreaseRanking(messageWorker: String) {
        val executeRedisCommand = redisDatabase.executeRedisCommand { c ->
            c.zaddincr("message_workers", -1.0, messageWorker)

        }
        println("Ranking decreased - result: $executeRedisCommand")
    }

    override fun increaseRanking(messageWorker: String) {
        val executeRedisCommand = redisDatabase.executeRedisCommand { c ->
            c.zaddincr("message_workers", 1.0, messageWorker)

        }
        println("Ranking increased - result: $executeRedisCommand")
    }

    override fun removeSelf(messageWorker: String) {
        val executeRedisCommand = redisDatabase.executeRedisCommand { c ->
            c.zrem("message_workers", messageWorker)
        }
        println("Removed self from active worker - result: $executeRedisCommand")
    }
}