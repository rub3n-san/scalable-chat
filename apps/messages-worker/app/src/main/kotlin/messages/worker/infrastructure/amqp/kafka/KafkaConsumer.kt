package messages.worker.infrastructure.amqp.kafka

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*

class KafkaConsumer(properties: Properties, instanceId: Long) {
    private val url: String = properties.getProperty("amqp.kafka.url", "localhost:9092")
    private val groupId = properties.getProperty("amqp.kafka.groupid", "chat-content-group")
    private val consumer: KafkaConsumer<String, ByteArray>
    private val json: Json
    init {
        val selfGroupId = "$groupId-$instanceId"
        val selfInstanceId = "message-worker-$instanceId"
        val selfClientId = "message-worker-client-$instanceId"
        val kafkaProps = Properties()
        kafkaProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = url // Kafka broker address
        kafkaProps[ConsumerConfig.GROUP_ID_CONFIG] = selfGroupId
        kafkaProps[ConsumerConfig.CLIENT_ID_CONFIG] = selfClientId
        kafkaProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        kafkaProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java.name

        // Set the instance id
        kafkaProps[ConsumerConfig.GROUP_INSTANCE_ID_CONFIG] = selfInstanceId
        println("Kafka consumer created with instance id $selfInstanceId on groupId $selfGroupId")
        consumer = KafkaConsumer<String, ByteArray>(kafkaProps)

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

    fun listen(): Map<String, List<BaseMessage>> {
        return consumer.poll(Duration.ofMillis(200)).map {
            try {
                val baseMessage: BaseMessage = json.decodeFromString(String(it.value()))
                Pair(it.topic(), baseMessage)
            }catch (e: Exception){
                println(e)
                println("Error while processing kafka message. topic ${it.topic()} message: ${String(it.value())}")
                Pair("unknown", ErrorMessage())
            }

        }.groupBy(
            keySelector = { it.first },
            valueTransform = { it.second }
        )
    }

    fun updateSubscriptions(channels: Set<String>) {
        consumer.subscribe(channels)
        println("Worker subscribed to ${consumer.subscription()} channels.")
    }
}