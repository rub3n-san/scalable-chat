package ws.server.infrastructure.databases.mongodb

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import messages.worker.domain.model.ChatContent
import org.bson.Document
import org.bson.types.ObjectId
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

    fun createClient(): MongoClient = MongoClients.create(
        MongoClientSettings.builder()
            .applyToClusterSettings { b -> b.hosts(listOf(ServerAddress(url))) }
            .credential(createPlainCredential).build())

}

class ChatContentStoreImpl(val mongoDb: MongoDb) : ChatContentStore {

    override fun fetchContent(documentIds: List<String>): List<ChatContent> {
        val client = mongoDb.createClient()

        val database = client.getDatabase(mongoDb.databaseName)
        val collection = database.getCollection(mongoDb.colllection)

        val objectIds = documentIds.map { ObjectId(it) }
        val query = Document("_id", Document("\$in", objectIds))

        try {
            return collection.find(query).map { ChatContent.fromMongoDbDocument(it) }.toList()
        } catch (e: Exception) {
            throw e
        } finally {
            client.close()
        }
    }

}