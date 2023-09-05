package system.test.integration

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketClient {
    private val client = OkHttpClient()
    fun createWebSocket(url: String, channelName: String, user: String): StatefulSession {
        val request = Request.Builder()
            .url(url)
            .build()
        //print("connecting to $request")

        val stateWebSocketListener = StateWebSocketListener(channelName, user)
        val websocket = client.newWebSocket(request, stateWebSocketListener)
        return StatefulSession(websocket, stateWebSocketListener)
    }

}

class StatefulSession(val websocket: WebSocket, val state: StateWebSocketListener) {
    val states = state.store
}

class StateWebSocketListener(val channelName: String, val user: String) : WebSocketListener() {
    val store: MutableList<String> = mutableListOf()
    override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
        //println("WebSocket connection opened: ${response.request.url}")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        //println("Received message on ${webSocket.request().url} [$channelName] $[$user]: $text")
        store.add(text)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        //println("WebSocket closed: $code - $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        //println("WebSocket connection failure: ${t.message}")
    }
}