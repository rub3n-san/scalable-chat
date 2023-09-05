package messages.worker.domain.services

import io.ktor.websocket.*
import io.mockk.clearAllMocks
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class WebsocketManagerTest {

    private lateinit var session: WebSocketSession

    @BeforeEach
    fun setUp() {
        session = mockk()
        clearAllMocks()
    }

    @Test
    fun `addSession adds a session to the websocketSessions`() {
        val channelName = "testChannel"
        val userName = "testUser"

        WebsocketManager.addSession(channelName, userName, session)

        val sessions = WebsocketManager.listSessions(channelName)
        Assertions.assertTrue(sessions.any { it.userName == userName && it.websocket == session })
    }

    @Test
    fun `listSessions returns the set of sessions for a given channel`() {
        val channelName = "testChannel"
        val userName = "testUser"
        WebsocketManager.addSession(channelName, userName, session)

        val sessions = WebsocketManager.listSessions(channelName)

        Assertions.assertTrue(sessions.any { it.userName == userName && it.websocket == session })
    }

    @Test
    fun `removeSession removes a session from the websocketSessions`() {
        val channelName = "testChannel"
        val userName = "testUser"
        WebsocketManager.addSession(channelName, userName, session)

        WebsocketManager.removeSession(channelName, userName)

        val sessions = WebsocketManager.listSessions(channelName)
        Assertions.assertTrue(sessions.isEmpty())
    }

    @Test
    fun `checkForNewSubscriptions returns true and new keys if keys changed`() {
        WebsocketManager.addSession("channel1", "user1", mockk())
        WebsocketManager.addSession("channel2", "user2", mockk())

        val (keysChanged, currentKeys) = WebsocketManager.checkForNewSubscriptions()

        Assertions.assertTrue(keysChanged)
        Assertions.assertTrue(currentKeys.contains("channel1"))
        Assertions.assertTrue(currentKeys.contains("channel2"))
    }

    @Test
    fun `checkForNewSubscriptions returns false and empty set if keys not changed`() {
        WebsocketManager.reset()
        val (keysChanged, currentKeys) = WebsocketManager.checkForNewSubscriptions()

        Assertions.assertFalse(keysChanged)
        Assertions.assertTrue(currentKeys.isEmpty())
    }

    @Test
    fun `hasSubscriptions returns false when there are no subscriptions`() {
        WebsocketManager.reset()
        val hasSubscriptions = WebsocketManager.hasSubscriptions()

        Assertions.assertFalse(hasSubscriptions)
    }


    @Test
    fun `hasSubscriptions returns true when there are subscriptions`() {
        WebsocketManager.addSession("channel1", "user1", mockk())

        val hasSubscriptions = WebsocketManager.hasSubscriptions()

        Assertions.assertTrue(hasSubscriptions)
    }

}
