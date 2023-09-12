package ws.server.domain.services

import messages.worker.infrastructure.amqp.kafka.KafkaProducerImpl
import ws.server.api.model.PostMessageDto
import ws.server.domain.model.ChatContent
import ws.server.domain.model.Message
import ws.server.infrastructure.databases.mongodb.ChatContentStore
import ws.server.infrastructure.databases.postgres.Channel
import ws.server.infrastructure.databases.postgres.Member
import ws.server.infrastructure.databases.postgres.MetadataStore
import ws.server.infrastructure.databases.postgres.User
import java.time.LocalDateTime

class ChannelService(
    val metadataStore: MetadataStore,
    val chatContentStore: ChatContentStore,
    val userService: UserService,
    val kafka: KafkaProducerImpl
) {
    fun listMessages(channelName: String, offset: Int, limit: Int): List<Message> {

        val channel = createChannelIfDoesNotExists(channelName)

        val latestMessages = metadataStore.listLatestMessagesPaginated(channel, offset, limit)

        val fetchedContent: List<ChatContent> = chatContentStore.fetchContent(latestMessages.map { it.documentId })

        println(fetchedContent)

        return fetchedContent.map {
            Message(
                content = it.content,
                createdAt = it.createdAt,
                userName = it.userName
            )
        }
    }

    fun send(channelName: String, userName: String, message: PostMessageDto) {
        val channel = createChannelIfDoesNotExists(channelName)
        val user = userService.createUserIfDoesNotExists(userName)

        val createdAt = LocalDateTime.now()
        val documentId =
            chatContentStore.saveContent(
                content = message.content,
                channelName = channelName,
                userName = userName,
                createdAt = createdAt
            )

        metadataStore.createChat(channel = channel, user = user, documentId = documentId)
        kafka.publishNewMessage(channelName, documentId)
    }

    fun addMember(channel: Channel, user: User): Member {
        return metadataStore.addMember(channel, user)
    }

    fun listActiveMembers(channel: Channel): List<User>{
        return metadataStore.listConnectedMembers(channel)
    }

    fun setConnected(channelName: String, userName: String, memberId: Long, connected: Boolean){
        println("setConnected $channelName, $userName, $memberId, $connected")
        try{
            metadataStore.setConnected(memberId, connected)
            if(connected){
                kafka.memberConnected(channelName, userName, memberId)
            }else{
                kafka.memberDisconnected(channelName, userName, memberId)
            }
        } catch (e: Exception){
            println(e)
        }

    }

    fun createChannelIfDoesNotExists(channelName: String): Channel =
        metadataStore.findChannel(channelName) ?: createChannel(channelName)

    private fun createChannel(channelName: String): Channel {
        val channel = metadataStore.createChannel(channelName)
        println("Channel $channelName created.")
        return channel
    }


}