package ws.server.infrastructure.databases.postgres


interface MetadataStore {

    fun createChannel(channel: String): Channel
    fun findChannel(channel: String): Channel?
    fun listLatestMessagesPaginated(channel: Channel, offset: Int, limit: Int): List<Chat>
    fun createChat(channel: Channel, user: User, documentId: String): Int
    fun findUser(user: String): User?
    fun createUser(userName: String): User
    fun addMember(channel: Channel, user: User, connected: Boolean = false): Member
    fun setConnected(memberId: Long, connected: Boolean)
    fun listConnectedMembers(channel: Channel): List<User>
}