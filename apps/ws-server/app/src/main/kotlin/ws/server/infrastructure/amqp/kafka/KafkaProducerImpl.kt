package messages.worker.infrastructure.amqp.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import ws.server.infrastructure.amqp.kafka.*
import java.util.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class KafkaProducerImpl(properties: Properties) {
    private val url: String = properties.getProperty("amqp.kafka.url", "localhost:9092")
    private val producer: KafkaProducer<String, ByteArray>
    private val json: Json

    init {
        val properties = Properties()
        properties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = url
        properties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        properties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java.name
        producer = KafkaProducer<String, ByteArray>(properties)
        json = Json {
            serializersModule = SerializersModule {
                polymorphic(BaseMessage::class) {
                    subclass(NewMessage::class)
                    subclass(MemberConnected::class)
                    subclass(MemberDisconnected::class)
                }
            }
        }
    }

    fun publishNewMessage(channelName: String, documentId: String) {
        val newMessage: BaseMessage = NewMessage(documentId =documentId)
        val json = json.encodeToString(newMessage)
        val serializedChat = json.toByteArray()
        val record = ProducerRecord<String, ByteArray>(channelName, serializedChat)
        println("Publishing new message topic: $channelName - $json")
        producer.send(record)
        producer.flush()
    }

    fun memberConnected(channelName: String, userName: String, memberId: Long){
        val memberConnected: BaseMessage = MemberConnected(memberId, userName )
        val json = json.encodeToString(memberConnected)
        val serializedNewMember = json.toByteArray()
        val record = ProducerRecord<String, ByteArray>(channelName, serializedNewMember)
        println("Publishing member connected to topic: $channelName - $json")
        producer.send(record)
        producer.flush()
    }

    fun memberDisconnected(channelName: String, userName: String, memberId: Long){
        val memberDisconnected: BaseMessage = MemberDisconnected(memberId, userName )
        val json = json.encodeToString(memberDisconnected)
        val serializedMemberDisconnected = json.toByteArray()
        val record = ProducerRecord<String, ByteArray>(channelName, serializedMemberDisconnected)
        println("Publishing member disconnected to topic: $channelName - $json")
        producer.send(record)
        producer.flush()
    }
}