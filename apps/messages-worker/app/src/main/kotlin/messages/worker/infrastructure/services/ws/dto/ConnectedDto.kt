package messages.worker.infrastructure.services.ws.dto

import kotlinx.serialization.Serializable

@Serializable
data class ConnectedDto(val memberId: Long, val connected: Boolean)
