package messages.worker.domain.services

import messages.worker.infrastructure.services.ws.service.WSServerClient

class PostChatService(wsServerHost: String) {
    val ws = WSServerClient("http://$wsServerHost/chat")

    fun postChat(channel: String, user: String, content: String) = ws.postMessage(channel, user, content)

}