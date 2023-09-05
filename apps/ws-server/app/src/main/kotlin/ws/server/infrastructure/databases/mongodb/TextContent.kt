package ws.server.infrastructure.databases.mongodb

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class TextContent(@BsonId val id: ObjectId, val content: String)
