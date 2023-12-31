/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package messages.worker

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import messages.worker.api.configureWebSockets
import messages.worker.domain.services.MessageWorkerRankingService
import messages.worker.domain.services.MessageWorkerService
import messages.worker.domain.services.PostChatService
import messages.worker.domain.services.WebsocketManager
import messages.worker.infrastructure.amqp.kafka.KafkaConsumer
import messages.worker.infrastructure.databases.redis.MessageWorkerRankingStoreImpl
import messages.worker.infrastructure.databases.redis.RedisDatabase
import ws.server.infrastructure.databases.mongodb.ChatContentStoreImpl
import ws.server.infrastructure.databases.mongodb.MongoDb
import java.net.InetAddress
import java.util.*

class MessagesWorkerApp {
    val greeting: String
        get() {
            return "Hello I am a message worker!"
        }
}

fun getLocalIpAddress(): String? {
    try {
        val localhost = InetAddress.getLocalHost()
        return localhost.hostAddress
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun main() {
    println(MessagesWorkerApp().greeting)


    val DEFAULT_CONFIG_FILE = "application.properties"
    //if (isLocal) "application.properties" //"apps/messages-worker/app/src/main/resources/application.properties"
    //else "/usr/src/app/app/src/main/resources/application.properties"
    val properties = Properties()
    val propertiesStream = Thread.currentThread().contextClassLoader.getResourceAsStream(DEFAULT_CONFIG_FILE)
    properties.load(propertiesStream)

    val APP_CONFIG_FILE = System.getenv("APP_CONFIG_FILE")
    if (APP_CONFIG_FILE != null) {
        val specificEnvProperties = envSpecificProperties(APP_CONFIG_FILE)
        // Override default properties with environment-specific properties
        properties.putAll(specificEnvProperties)
    }

    val webserverPort = properties.getProperty("app.port", "9090").toInt()

    val isDockerEnv = properties.getProperty("app.docker", "false").toBoolean()
    println("isDockerEnv: $isDockerEnv")

    val localIp = if (isDockerEnv) getLocalIpAddress() else "localhost"
    println("Self ip address is: $localIp")

    val EXPOSED_PORT = System.getenv("EXPOSED_PORT")
    val port = if (EXPOSED_PORT == null || EXPOSED_PORT.isEmpty()) webserverPort else EXPOSED_PORT
    val selfUrl = "$localIp:$port"
    println("Self url is: $selfUrl")

    val redisDatabase = RedisDatabase(properties)
    val messageWorkerRankingStoreImpl = MessageWorkerRankingStoreImpl(redisDatabase, selfUrl)

    val messageWorkerRankingService = MessageWorkerRankingService(messageWorkerRankingStoreImpl, selfUrl)

    val kafkaConsumer = KafkaConsumer(properties, messageWorkerRankingService.selfNumber)
    val localUserSessionManager = WebsocketManager

    val postChatService = PostChatService(properties.getProperty("wsserver.url", "localhost:8080"))

    val mongoDb = MongoDb(properties)
    val chatContentStoreImpl = ChatContentStoreImpl(mongoDb)
    val messageWorkerService: MessageWorkerService = MessageWorkerService(
        kafkaConsumer = kafkaConsumer,
        websocketManager = localUserSessionManager,
        messageWorkerRankingService = messageWorkerRankingService,
        chatContentStore = chatContentStoreImpl,
        postChatService = postChatService

    )



    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        println("Uncaught exception: ${e.message}")
        // Perform cleanup tasks here
    }


    val job = GlobalScope.launch {
        messageWorkerService.work()
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking { job.cancelAndJoin() }
        messageWorkerRankingService.removeSelf()
    })

    //Initialize webserver
    println("Initializing  webserver on port $webserverPort...")
    embeddedServer(Netty, port = webserverPort) {
        configureWebSockets(
            messageWorkerService, postChatService
        )
    }.start(wait = true)


}

private fun envSpecificProperties(APP_CONFIG_FILE: String): Properties {
    // Load environment-specific properties (e.g., application-docker.properties)
    val envProperties = Properties()
    println("Using environment-specific config file: $APP_CONFIG_FILE")
    val specificEnvFile = Thread.currentThread().contextClassLoader.getResourceAsStream(APP_CONFIG_FILE)
    envProperties.load(specificEnvFile)
    return envProperties
}

