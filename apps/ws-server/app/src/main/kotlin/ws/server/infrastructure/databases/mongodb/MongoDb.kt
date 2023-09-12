package ws.server.infrastructure.databases.mongodb

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.Document
import org.bson.types.ObjectId
import ws.server.domain.model.ChatContent
import java.time.LocalDateTime
import java.util.*

class MongoDb(properties: Properties) {

    val url = properties.getProperty("database.mongodb.url", "localhost:27018")
    val username = properties.getProperty("database.mongodb.username", "root")
    val source = properties.getProperty("database.mongodb.source", "admin")
    val password = properties.getProperty("database.mongodb.password", "XXXX")
    val databaseName = properties.getProperty("database.mongodb.databasename", "chat-db")
    val colllection = properties.getProperty("database.mongodb.colllection", "text_content")
    private val createPlainCredential = MongoCredential.createScramSha256Credential(
        username,
        source,
        password.toCharArray()
    )

    private val _client: MongoClient by lazy {
        MongoClients.create(
            MongoClientSettings.builder()
                .applyToClusterSettings { b -> b.hosts(listOf(ServerAddress(url))) }
                .credential(createPlainCredential).build())
    }

    fun getMongoClient(): MongoClient = _client

}

class ChatContentDbImpl(mongoDb: MongoDb) : ChatContentStore {
    private val client = mongoDb.getMongoClient()
    private val database = client.getDatabase(mongoDb.databaseName)
    private val collection = database.getCollection(mongoDb.colllection)

    override fun fetchContent(documentIds: List<String>): List<ChatContent> {
        val objectIds = documentIds.map { ObjectId(it) }
        val query = Document("_id", Document("\$in", objectIds))
        return collection.find(query).map { ChatContent.fromMongoDbDocument(it) }.toList()
    }

    override fun saveContent(content: String, channelName: String, userName: String, createdAt: LocalDateTime): String {
        val insertedId = collection.insertOne(
            ChatContent.toMongoDbDocument(content, channelName, userName, createdAt)
        ).insertedId

        println("Chat document inserted with _id [$insertedId]")
        return insertedId?.asObjectId()?.value.toString()
    }
}