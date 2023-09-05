package messages.worker.infrastructure.amqp.kafka

import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

class KafkaProducerImpl(properties: Properties) {
    private val url: String = properties.getProperty("amqp.kafka.url", "localhost:9092")

    private val producer: KafkaProducer<String, ByteArray>

    init {
        val properties = Properties()
        properties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = url
        properties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        properties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java.name
        producer = KafkaProducer<String, ByteArray>(properties)
    }

    fun publishNewMessage(channelName: String, documentId: String) {
        val chat = Chat(documentId)
        val json = Json.encodeToString(Chat.serializer(), chat)
        val serializedChat = json.toByteArray()

        val record = ProducerRecord<String, ByteArray>(channelName, serializedChat)
        println("Publishing new message $channelName - $documentId")
        producer.send(record)
        producer.flush()
    }
}