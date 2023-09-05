package ws.server.domain.model

data class MessageWorker(val url: String) {
    var websocket: String

    init {
        websocket = "ws://$url"
    }
}
