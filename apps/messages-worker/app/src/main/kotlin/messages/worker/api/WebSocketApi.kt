package messages.worker.api

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import messages.worker.domain.services.MessageWorkerService
import messages.worker.domain.services.PostChatService
import java.time.Duration

fun Application.configureWebSockets(
    messageWorkerService: MessageWorkerService,
    postChatService: PostChatService
) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/connect/{channel}") {
            //TODO: Validate connection
            val user = call.parameters["user"].toString() // Accessing the parameter from the path
            val channel = call.parameters["channel"] ?: "delete"

            println("User $user connected to channel $channel!")

            messageWorkerService.connect(channel, user, this)

            try {
                send("connected!")
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val receivedText = frame.readText()
                            //This is here for testing purposes, the client should post a new message/chat directly through the endpoint and not via websocket
                            postChatService.postChat(channel, user, receivedText)
                        }
                        // Handle other types of frames if needed
                        else -> {
                            println(frame)
                        }
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Removing $user from active user!")
                close(CloseReason(CloseReason.Codes.NORMAL, "Closing the connection"))
                messageWorkerService.disconnect(channel, user)
            }
        }
    }
}

