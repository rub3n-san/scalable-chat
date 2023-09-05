package system.test.integration.model

import kotlinx.serialization.Serializable


@Serializable
data class WebSocketDto(val baseWebsocket: String, val websocket: String, val user: String, val channel: String)