package messages.worker.domain.services

import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import messages.worker.domain.model.BaseWebSocketMessage
import messages.worker.domain.model.WSChatContent
import messages.worker.domain.model.WSMemberDisconnected
import messages.worker.domain.model.WSNewMemberConnected
import messages.worker.infrastructure.amqp.kafka.*
import ws.server.infrastructure.databases.mongodb.ChatContentStore

class MessageWorkerService(
    val kafkaConsumer: KafkaConsumer,
    val websocketManager: WebsocketManager,
    val messageWorkerRankingService: MessageWorkerRankingService,
    val chatContentStore: ChatContentStore,
    val postChatService: PostChatService
) {

    fun connect(channelName: String, userName: String, memberId: Long, websocket: WebSocketSession) {
        websocketManager.addSession(channelName, userName, memberId, websocket)
        messageWorkerRankingService.increase()
        postChatService.setConnected(channelName, userName, memberId, true)
    }

    fun disconnect(channelName: String, userName: String, memberId: Long) {
        websocketManager.removeSession(channelName, userName)
        messageWorkerRankingService.decrease()
        postChatService.setConnected(channelName, userName, memberId, false)
    }
    val json = Json {
        serializersModule = SerializersModule {
            polymorphic(BaseWebSocketMessage::class) {
                subclass(WSChatContent::class)
                subclass(WSMemberDisconnected::class)
                subclass(WSNewMemberConnected::class)
            }
        }
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
                    val messages = kafkaConsumer.listen()

                    messages.forEach { (channel, baseMessages) ->

                        println("Processing ${baseMessages.size} messages for channel $channel")
                        //scope.launch(Dispatchers.Default) {
                        val sessions = websocketManager.listSessions(channel)
                        println("Sessions for channel ($channel) - ${sessions.map { it.userName }}")

                        for (baseMessage in baseMessages){
                            when(baseMessage){
                                is NewMessage -> {
                                    val documentId = arrayListOf(baseMessage.documentId)
                                    val chatContent = chatContentStore.fetchContent(documentId)
                                    sessions.forEach { session ->
                                        //scope.launch(Dispatchers.Default) {
                                        sendMessagesToSession(session, chatContent)
                                        //}
                                    }
                                    //}
                                }
                                is MemberConnected -> {
                                    val wsMemberConnected = WSNewMemberConnected(userName = baseMessage.userName)
                                    sessions.forEach { session ->
                                        //scope.launch(Dispatchers.Default) {
                                        sendMessagesToSession(session, arrayListOf(wsMemberConnected))
                                        //}
                                    }
                                }
                                is MemberDisconnected -> {
                                    val wsMemberDisconnected = WSMemberDisconnected(userName = baseMessage.userName)
                                    sessions.forEach { session ->
                                        //scope.launch(Dispatchers.Default) {
                                        sendMessagesToSession(session, arrayListOf(wsMemberDisconnected))
                                        //}
                                    }
                                }

                                else -> {}
                            }
                        }

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

    private suspend fun sendMessagesToSession(session: Session, chatContents: List<BaseWebSocketMessage>) {
        chatContents//.filter { chat -> chat.userName != session.userName }
            .forEach { chat ->
                //println("Sending message for user ${session.userName}")
                //session.websocket.send("${chat.userName}: ${chat.content}  [${chat.createdAt}]")
                val requestBody = json.encodeToString(chat)
                println("sending to session ${session.userName} -> $requestBody")
                session.websocket.send(requestBody)
            }
    }
}