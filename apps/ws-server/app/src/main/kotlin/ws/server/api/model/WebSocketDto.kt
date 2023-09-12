package ws.server.api.model

import kotlinx.serialization.Serializable
import ws.server.domain.model.MessageWorker
import ws.server.infrastructure.databases.postgres.Channel
import ws.server.infrastructure.databases.postgres.Member
import ws.server.infrastructure.databases.postgres.User

@Serializable
data class WebSocketDto(val baseWebsocket: String, val websocket: String, val user: String, val channel: String, val memberId: Long) {
    companion object {
        fun toApi(messageWorker: MessageWorker, user: User, channel: Channel, member: Member) =
            WebSocketDto(
                messageWorker.websocket,
                "${messageWorker.websocket}/connect/${channel.name}?user=${user.name}&memberId=${member.id}",
                user.name,
                channel.name,
                member.id
            )
    }
}
