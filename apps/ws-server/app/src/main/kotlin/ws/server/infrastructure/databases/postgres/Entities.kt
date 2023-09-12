package ws.server.infrastructure.databases.postgres

import org.ktorm.entity.Entity
import java.time.LocalDateTime

interface User: Entity<User> {
    companion object: Entity.Factory<User>()
    var id: Long
    var name:String
    val createdAt: LocalDateTime
    var lastLogin: LocalDateTime
}
interface Channel : Entity<Channel>{
    companion object: Entity.Factory<Channel>()
    val id: Long
    var name: String
    val createdAt: LocalDateTime
}

interface Member : Entity<Member>{
    companion object: Entity.Factory<Member>()
    val id: Long
    var userId: Long
    var channelId: Long
    var connected: Boolean
    val createdAt: LocalDateTime
}

interface Chat : Entity<Chat>{
    companion object: Entity.Factory<Chat>()
    val id: Long
    var userId: Long
    var channelId: Long
    var documentId: String
    val createdAt: LocalDateTime
}