package ws.server.domain.model

import java.time.LocalDateTime

data class Message(val content: String, val createdAt: LocalDateTime, val userName: String)
