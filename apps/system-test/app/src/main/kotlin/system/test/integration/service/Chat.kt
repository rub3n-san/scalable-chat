package system.test.integration.service

import system.test.integration.ChatApiClient
import system.test.integration.StatefulSession
import system.test.integration.WebSocketClient

class Chat(val chatApi: ChatApiClient, val webSocketClient: WebSocketClient) {
    fun connect(channel: String, user: String): StatefulSession {
        val url = chatApi.connect(channel, user, true)
        val localUrl = replaceIpAddress(url!!.webSocket.websocket, "localhost")
        return webSocketClient.createWebSocket(localUrl, channel, user)
    }

    fun replaceIpAddress(input: String, newIpAddress: String): String {
        val regex = Regex("(ws://)(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(:)")
        return regex.replace(input) { matchResult ->
            "${matchResult.groupValues[1]}$newIpAddress${matchResult.groupValues[3]}"
        }
    }

}