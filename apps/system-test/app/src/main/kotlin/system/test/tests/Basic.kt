package system.test.tests

import system.test.integration.ChatApiClient
import system.test.integration.service.Chat
import system.test.tests.base.TestCase
import system.test.tests.base.TestSuite

class Basic(override val chatApi: ChatApiClient, override val chat: Chat) :
    TestSuite("Basic", chatApi, chat) {

    private val channel = "Basic"
    private val numberOfUsers = 6

    private val allConnected = object : TestCase("assert all users get connected", this) {

        override fun test(): String {
            val sessions = statefulSessions(channel, numberOfUsers)

            val allConnected =
                sessions.all { it.state.store.all { storeValue -> storeValue == "connected!" } }

            sessions.forEach { it.websocket.cancel() }

            return if (allConnected)
                "OK"
            else
                "NOK" + sessions.map { it.states }

        }
    }

    private val receiveMessage = object : TestCase("all users receive a message", this) {

        override fun test(): String {
            val sessions = statefulSessions(channel, numberOfUsers)

            chatApi.postMessage(channel, "test", "message")


            Thread.sleep(5000)


            val find = sessions.find { it.state.store.size != 2 }

            sessions.forEach { it.websocket.cancel() }
            return if (find != null)
                "NOK" + sessions.map { it.states }
            else
                "OK"
        }
    }

    init {
        addTestCase(allConnected)
        addTestCase(receiveMessage)
    }


    override fun flush() {
        statefulSessions(channel, numberOfUsers)
    }

}