package system.test.integration

import com.google.common.net.HttpHeaders
import fuel.Fuel
import fuel.method
import fuel.post
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import system.test.integration.model.ConnectDto
import system.test.integration.model.PostMessageDto


class ChatApiClient(private val baseUrl: String) {

    fun postMessage(channel: String, user: String, content: String) {
        val messageDto = PostMessageDto(content)
        val requestHeaders = mapOf("user" to user, HttpHeaders.CONTENT_TYPE to "application/json")
        val requestBody = Json.encodeToString(PostMessageDto.serializer(), messageDto)

        //println("$baseUrl/chat/$channel")
        //println(requestBody)
        val statusCode = runBlocking {
            Fuel.post("$baseUrl/chat/$channel", body = requestBody, headers = requestHeaders)
        }.statusCode

        return when (statusCode) {
            200 -> println("Message posted!")
            else -> println("Unable to post message, http status: $statusCode")
        }
    }

    fun connect(channel: String, user: String, snapshot: Boolean): ConnectDto? {
        val requestHeaders = mapOf("user" to user, HttpHeaders.CONTENT_TYPE to "application/json")

        //println("$baseUrl/connect/$channel?snapshot=$snapshot")
        val response = runBlocking {
            Fuel.method(
                url = "$baseUrl/connect/$channel?snapshot=$snapshot",
                method = "GET",
                headers = requestHeaders
            )
        }

        return when (response.statusCode) {
            200 -> Json.decodeFromString<ConnectDto>(response.body)
            else -> {
                println("Unable to connect, http status: ${response.statusCode}")
                return null
            }
        }
    }

}