package ws.server.domain.services

import ws.server.api.model.ChatDto
import ws.server.api.model.ConnectDto
import ws.server.api.model.MessageDto
import ws.server.api.model.WebSocketDto

class ChatService(
    val channelService: ChannelService,
    val userService: UserService,
    val messageWorkersService: MessageWorkersService
) {

    suspend fun processMessageAsync(userName: String, channelName: String, snapshot: Boolean): ConnectDto {
        println("User $userName wants to connect to channel $channelName...")
        val user = userService.createUserIfDoesNotExists(userName)
        val channel = channelService.createChannelIfDoesNotExists(channelName)

        val messageWorker = messageWorkersService.findSuitableMessageWorker()

        var chats: ChatDto? = null
        if (snapshot) {
            val listMessages = channelService.listMessages(channelName, 0, 10)
            chats = ChatDto(messages = listMessages.map { MessageDto.toApi(it) })
        }

        return ConnectDto(webSocket = WebSocketDto.toApi(messageWorker, user, channel), chat = chats)
    }
}