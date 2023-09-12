package ws.server.domain.services

import ws.server.api.model.*
import ws.server.infrastructure.databases.postgres.User

class ChatService(
    val channelService: ChannelService,
    val userService: UserService,
    val messageWorkersService: MessageWorkersService
) {

    suspend fun processMessageAsync(userName: String, channelName: String, snapshot: Boolean): ConnectDto {
        println("User $userName wants to connect to channel $channelName...")
        val user = userService.createUserIfDoesNotExists(userName)
        val channel = channelService.createChannelIfDoesNotExists(channelName)
        val member = channelService.addMember(channel, user)
        val messageWorker = messageWorkersService.findSuitableMessageWorker()

        var chats: ChatDto? = null
        var listActiveMembers: List<User> = emptyList()
        if (snapshot) {
            val listMessages = channelService.listMessages(channelName, 0, 10)
            chats = ChatDto(messages = listMessages.map { MessageDto.toApi(it) })
            listActiveMembers = channelService.listActiveMembers(channel)
        }

        return ConnectDto(webSocket = WebSocketDto.toApi(messageWorker, user, channel, member), chat = chats, activeMembers = listActiveMembers.map { UserDto( it.name) })
    }
}