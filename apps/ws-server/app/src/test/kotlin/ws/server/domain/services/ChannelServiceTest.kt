package ws.server.domain.services

import io.mockk.every
import io.mockk.mockk
import messages.worker.infrastructure.amqp.kafka.KafkaProducerImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ws.server.domain.model.ChatContent
import ws.server.infrastructure.databases.mongodb.ChatContentStore
import ws.server.infrastructure.databases.postgres.Chat
import ws.server.infrastructure.databases.postgres.MetadataStore
import java.time.LocalDateTime

class ChannelServiceTest {

    private lateinit var metadataStore: MetadataStore
    private lateinit var chatContentStore: ChatContentStore
    private lateinit var userService: UserService
    private lateinit var kafka: KafkaProducerImpl

    private lateinit var channelService: ChannelService

    @BeforeEach
    fun setUp() {
        metadataStore = mockk()
        chatContentStore = mockk()
        userService = mockk()
        kafka = mockk()

        channelService = ChannelService(metadataStore, chatContentStore, userService, kafka)
    }

    @Test
    fun `listMessages returns list of messages`() {
        val channelName = "testChannel"
        val offset = 0
        val limit = 10
        val chatContentList = listOf(
            ChatContent("user1", "Message 1", "", "", LocalDateTime.now()),
            ChatContent("user2", "Message 2", "", "", LocalDateTime.now())
        )
        val latestMessages = chatContentList.mapIndexed { index, chatContent ->
            Chat { userId = 1 }//("Message $index", chatContent.createdAt, chatContent.userName)
        }

        every { metadataStore.findChannel(channelName) } returns mockk()
        every { metadataStore.listLatestMessagesPaginated(any(), offset, limit) } returns latestMessages
        every { chatContentStore.fetchContent(any()) } returns chatContentList

        val messages = channelService.listMessages(channelName, offset, limit)

        Assertions.assertEquals(latestMessages.size, messages.size)
    }

}