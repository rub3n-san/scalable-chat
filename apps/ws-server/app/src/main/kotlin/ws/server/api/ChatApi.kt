package ws.server.api

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.serialization.json.Json
import messages.worker.infrastructure.amqp.kafka.KafkaProducerImpl
import sun.security.pkcs.ParsingException
import ws.server.api.model.ConnectedDto
import ws.server.api.model.ExceptionResponse
import ws.server.api.model.MessageDto
import ws.server.api.model.PostMessageDto
import ws.server.domain.services.ChannelService
import ws.server.domain.services.ChatService
import ws.server.domain.services.MessageWorkersService
import ws.server.domain.services.UserService
import ws.server.infrastructure.databases.mongodb.ChatContentStore
import ws.server.infrastructure.databases.postgres.MetadataStore
import java.util.*
import javax.xml.bind.ValidationException


class ChatApi(
    val metadataStore: MetadataStore,
    val chatContentStore: ChatContentStore,
    val userService: UserService,
    val messageWorkersService: MessageWorkersService,
    val properties: Properties
) {
    fun Application.main() {
        install(ContentNegotiation) {
            json(Json { prettyPrint = true })
        }
        install(StatusPages) {
            exception<Throwable> { call, throwable ->
                when (throwable) {
                    is ValidationException -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ExceptionResponse("${throwable.message}", HttpStatusCode.BadRequest.value)
                        )
                    }

                    is ParsingException -> {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ExceptionResponse("${throwable.message}", HttpStatusCode.ExpectationFailed.value)
                        )
                    }

                    else -> {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ExceptionResponse("${throwable.message}", HttpStatusCode.ExpectationFailed.value)
                        )
                    }
                }
            }
        }
        val channelService = ChannelService(metadataStore, chatContentStore, userService, KafkaProducerImpl(properties))
        val chatService = ChatService(channelService, userService, messageWorkersService)

        routing {


            route("/connect") {
                get("{channel}") {
                    val channelName = call.parameters.getOrFail("channel")
                    val snapshot = call.parameters["snapshot"]?.toBoolean() ?: false
                    val userName = call.request.header("user").toString()

                    call.respond(chatService.processMessageAsync(userName, channelName, snapshot))
                }

            }
            route("/chat") {
                get("{channel}") {
                    val channelName = call.parameters.getOrFail("channel")
                    val user = call.request.header("user")

                    // Fetch user data from database
                    val info = "User $user joined channel $channelName"
                    println(info)

                    val listMessages = channelService.listMessages(channelName = channelName, offset = 0, limit = 10)

                    call.respond(listMessages.map { MessageDto.toApi(it) })
                }

                post("{channel}") {
                    val channelName = call.parameters.getOrFail("channel")
                    val user = call.request.header("user").toString()
                    val message = call.receive<PostMessageDto>()

                    channelService.send(
                        channelName = channelName,
                        userName = user,
                        message = message
                    )

                    call.respond(mapOf("received" to message.content))
                }
                post("{channel}/connected"){
                    val channelName = call.parameters.getOrFail("channel")
                    val user = call.request.header("user").toString()
                    val connected = call.receive<ConnectedDto>()
                    channelService.setConnected(channelName, user, connected.memberId, connected.connected)
                    call.respondText("OK")
                }
            }

        }
    }

}

