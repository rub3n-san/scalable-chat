package ws.server.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ConnectedDto(val memberId: Long, val connected: Boolean)
