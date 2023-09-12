package messages.worker.infrastructure.services.ws.service

import com.google.common.net.HttpHeaders.CONTENT_TYPE
import fuel.Fuel
import fuel.post
import fuel.put
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import messages.worker.infrastructure.services.ws.dto.ConnectedDto
import messages.worker.infrastructure.services.ws.dto.PostMessageDto

class WSServerClient(private val baseUrl: String) {


    fun postMessage(channel: String, user: String, content: String) {
        val messageDto = PostMessageDto(content)
        val requestHeaders = mapOf("user" to user, CONTENT_TYPE to "application/json")
        val requestBody = Json.encodeToString(PostMessageDto.serializer(), messageDto)

        val statusCode = runBlocking {
            Fuel.post("$baseUrl/$channel", body = requestBody, headers = requestHeaders)
        }.statusCode

        return when (statusCode) {
            200 -> println("Message posted!")
            else -> println("Unable to post message, http status: $statusCode")
        }
    }


    fun setConnected(channelName: String, user: String,memberId: Long, connected: Boolean){
        val connectedDto = ConnectedDto(memberId, connected)
        val requestHeaders = mapOf("user" to user, CONTENT_TYPE to "application/json")
        val requestBody = Json.encodeToString(ConnectedDto.serializer(), connectedDto)
        println(requestBody)
        val statusCode = runBlocking {
            Fuel.post("$baseUrl/$channelName/connected",  body = requestBody, headers = requestHeaders)
        }.statusCode

        when (statusCode) {
            200 -> println("Connection status updated!")
            else -> println("Unable to update connection status, http status: $statusCode")
        }
    }
}