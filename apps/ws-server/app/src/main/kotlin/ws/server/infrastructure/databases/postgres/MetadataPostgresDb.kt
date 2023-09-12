package ws.server.infrastructure.databases.postgres

import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.schema.*
import org.ktorm.support.postgresql.PostgreSqlDialect
import java.time.LocalDateTime
import java.util.*

object Users : Table<User>("t_user") {
    val id = long("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val created_at = datetime("created_at").bindTo { it.createdAt }
    val last_login = datetime("last_login").bindTo { it.lastLogin }
}

object Channels : Table<Channel>("t_channel") {
    val id = long("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val created_at = datetime("created_at").bindTo { it.createdAt }
}

object Members : Table<Member>("t_member") {
    val id = long("id").primaryKey().bindTo { it.id }
    val user_id = long("user_id").bindTo { it.userId }
    val channel_id = long("channel_id").bindTo { it.channelId }
    val connected = boolean("connected").bindTo { it.connected }
    val created_at = datetime("created_at").bindTo { it.createdAt }
}

object Chats : Table<Chat>("t_chat") {
    val id = long("id").primaryKey().bindTo { it.id }
    val user_id = long("user_id").bindTo { it.userId }
    val channel_id = long("channel_id").bindTo { it.channelId }
    val document_id = varchar("document_id").bindTo { it.documentId }
    val created_at = datetime("created_at").bindTo { it.createdAt }
}

class MetadataPostgresDb(properties: Properties) : MetadataStore {

    val database = Database.connect(
        url = properties.getProperty("database.postgres.url", "jdbc:postgresql://localhost:5432/postgres"),
        user = properties.getProperty("database.postgres.user", "postgres"),
        password = properties.getProperty("database.postgres.password", "XXXXX"),
        dialect = PostgreSqlDialect()
    )

    val Database.channels get() = this.sequenceOf(Channels)
    val Database.chats get() = this.sequenceOf(Chats)
    val Database.users get() = this.sequenceOf(Users)
    val Database.members get() = this.sequenceOf(Members)
    override fun createChannel(channel: String): Channel {
        database.channels.add(Channel { name = channel })
        val saved = findChannel(channel)!!
        println("Created channel $saved.")
        return saved
    }

    override fun findChannel(channel: String): Channel? =
        database.channels.find { it.name eq channel }


    override fun listLatestMessagesPaginated(channel: Channel, offset: Int, limit: Int): List<Chat> =
        database.chats
            .filter { it.channel_id eq channel.id }
            .sortedBy { it.created_at.desc() }
            .drop(offset)
            .take(limit)
            .toList()

    override fun createChat(channel: Channel, user: User, dId: String): Int =
        database.chats.add(Chat { userId = user.id; channelId = channel.id; documentId = dId })

    override fun findUser(user: String): User? = database.users.find { it.name eq user }
    override fun createUser(userName: String): User {
        database.users.add(User { name = userName; lastLogin = LocalDateTime.now() })
        val saved = findUser(userName)!!
        println("Created user $saved.")
        return saved
    }
    override fun addMember(channel: Channel, user: User, connected: Boolean): Member {
        return database.members.find {
            (it.user_id eq user.id) and (it.channel_id eq channel.id)
        } ?: run {
            Member {
                userId = user.id
                channelId = channel.id
                this.connected = connected
            }.also {
                database.members.add(it)
            }
        }

    }

    override fun setConnected(memberId: Long, connected: Boolean) {
        database.update(Members) {
            // Set the new value for the 'connected' column
            set(Members.connected, connected)
            // Where condition to specify which rows to update
            where { it.id eq memberId }
        }
    }

    fun countActiveMembers(channel: Channel): Int {
        return database.from(Members)
            .innerJoin(Users, on = Members.user_id eq Users.id)
            .select(count())
            .where { Members.connected eq true }.totalRecordsInAllPages
    }
    override fun listConnectedMembers(channel: Channel): List<User> {
        return database.from(Members)
            .innerJoin(Users, on = Members.user_id eq Users.id)
            .select(Users.id, Users.name, Users.created_at, Users.last_login)
            .where { Members.connected eq true }
            .map { row -> Users.createEntity(row) }
    }
}