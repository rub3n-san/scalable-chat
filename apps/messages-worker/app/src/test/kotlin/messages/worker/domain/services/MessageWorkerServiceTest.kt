package messages.worker.domain.services

import io.ktor.websocket.*
import io.mockk.*
import messages.worker.infrastructure.amqp.kafka.KafkaConsumer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ws.server.infrastructure.databases.mongodb.ChatContentStore

class MessageWorkerServiceTest {

    private lateinit var kafkaConsumer: KafkaConsumer
    private lateinit var websocketManager: WebsocketManager
    private lateinit var messageWorkerRankingService: MessageWorkerRankingService
    private lateinit var chatContentStore: ChatContentStore
    private lateinit var websocketSession: WebSocketSession

    private lateinit var messageWorkerService: MessageWorkerService

    @BeforeEach
    fun setUp() {
        kafkaConsumer = mockk()
        websocketManager = mockk()
        messageWorkerRankingService = mockk()
        chatContentStore = mockk()
        websocketSession = mockk()

        messageWorkerService = MessageWorkerService(
            kafkaConsumer,
            websocketManager,
            messageWorkerRankingService,
            chatContentStore
        )
    }

    @Test
    fun `connect adds session and increases ranking`() {
        val channelName = "testChannel"
        val userName = "testUser"

        every { websocketManager.addSession(channelName, userName, websocketSession) } just Runs
        every { messageWorkerRankingService.increase() } just Runs

        messageWorkerService.connect(channelName, userName, websocketSession)

        verify { websocketManager.addSession(channelName, userName, websocketSession) }
        verify { messageWorkerRankingService.increase() }
    }

    @Test
    fun `disconnect removes session and decreases ranking`() {
        val channelName = "testChannel"
        val userName = "testUser"

        every { websocketManager.removeSession(channelName, userName) } just Runs
        every { messageWorkerRankingService.decrease() } just Runs

        messageWorkerService.disconnect(channelName, userName)

        verify { websocketManager.removeSession(channelName, userName) }
        verify { messageWorkerRankingService.decrease() }
    }


}