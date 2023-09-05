package messages.worker.domain.services

import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import messages.worker.domain.model.ChatContent
import messages.worker.infrastructure.amqp.kafka.KafkaConsumer
import ws.server.infrastructure.databases.mongodb.ChatContentStore

class MessageWorkerService(
    val kafkaConsumer: KafkaConsumer,
    val websocketManager: WebsocketManager,
    val messageWorkerRankingService: MessageWorkerRankingService,
    val chatContentStore: ChatContentStore
) {

    fun connect(channelName: String, userName: String, websocket: WebSocketSession) {
        websocketManager.addSession(channelName, userName, websocket)
        messageWorkerRankingService.increase()
    }

    fun disconnect(channelName: String, userName: String) {
        websocketManager.removeSession(channelName, userName)
        messageWorkerRankingService.decrease()
    }

    suspend fun work() {
        val scope = CoroutineScope(Dispatchers.Default)

        try {
            println("Working...")
            while (true) {
                val updatedSubscriptionState = websocketManager.checkForNewSubscriptions()

                if (updatedSubscriptionState.first) {
                    kafkaConsumer.updateSubscriptions(updatedSubscriptionState.second)
                }
                if (websocketManager.hasSubscriptions()) {
                    val documentIdsGroupedByChannel = kafkaConsumer.fetchChats()

                    if (documentIdsGroupedByChannel.isEmpty().not()) {
                        println("Fetching chat content store for ${documentIdsGroupedByChannel.size} channels...")
                    }


                    documentIdsGroupedByChannel.forEach { (channel, documentIds) ->
                        println("Processing documents $documentIds for channel $channel")
                        //scope.launch(Dispatchers.Default) {
                        val sessions = websocketManager.listSessions(channel)
                        println("Sessions for channel ($channel) - ${sessions.map { it.userName }}")
                        val chatContent = chatContentStore.fetchContent(documentIds)
                        sessions.forEach { session ->
                            //scope.launch(Dispatchers.Default) {
                            sendMessagesToSession(session, chatContent)
                            //}
                        }
                        //}
                    }

                    scope.coroutineContext.cancelChildren()
                } else {
                    //println { "Waiting for subscriptions..." }
                    Thread.sleep(1000)
                }


            }
        } catch (e: Exception) {
            println(e)
        } finally {
            println("Finish for today... This might have interrupted some work... Needs to be fixed.")
        }
    }

    suspend fun sendMessagesToSession(session: Session, chatContent: List<ChatContent>) {
        chatContent.filter { chat -> chat.userName != session.userName }
            .forEach { chat ->
                //println("Sending message for user ${session.userName}")
                session.websocket.send("${chat.userName}: ${chat.content}  [${chat.createdAt}]")
            }
    }
}