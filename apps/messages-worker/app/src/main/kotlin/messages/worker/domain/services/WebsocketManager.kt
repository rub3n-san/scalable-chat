package messages.worker.domain.services

import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap


data class Session(val websocket: WebSocketSession, val userName: String, val memberId: Long)

object WebsocketManager {
    private var websocketSessions: ConcurrentHashMap<String, MutableSet<Session>> =
        ConcurrentHashMap<String, MutableSet<Session>>()
    private var lastKeys: Set<String> = emptySet()

    fun addSession(channelName: String, userName: String, memberId: Long, session: WebSocketSession) {
        val newSession = Session(session, userName, memberId)
        websocketSessions
            .getOrPut(channelName) { mutableSetOf() }
            .add(newSession)
    }

    fun listSessions(channelName: String): Set<Session> =
        websocketSessions[channelName] ?: emptySet()


    fun removeSession(channelName: String, userName: String) {
        websocketSessions[channelName]?.removeIf { it.userName == userName }
        if (websocketSessions[channelName]?.isEmpty() == true) {
            websocketSessions.remove(channelName)
            println("Channel $channelName removed")
        }
    }

    fun checkForNewSubscriptions(): Pair<Boolean, Set<String>> {
        val currentKeys = websocketSessions.keys.toSet()
        val keysChanged = currentKeys != lastKeys
        lastKeys = currentKeys
        return if (keysChanged) Pair(true, currentKeys) else Pair(false, emptySet())
    }

    fun hasSubscriptions(): Boolean =
        websocketSessions.isNotEmpty()

    fun reset() {
        websocketSessions = ConcurrentHashMap<String, MutableSet<Session>>()
        lastKeys = emptySet()
    }

}
