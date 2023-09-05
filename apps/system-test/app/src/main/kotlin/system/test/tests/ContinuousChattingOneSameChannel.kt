package system.test.tests

import system.test.integration.ChatApiClient
import system.test.integration.StatefulSession
import system.test.integration.service.Chat
import system.test.tests.base.TestCase
import system.test.tests.base.TestSuite

class ContinuousChattingOneSameChannel(override val chatApi: ChatApiClient, override val chat: Chat) :
    TestSuite("ContinuousChattingOneSameChannel", chatApi, chat) {

    val channel = "ContinuousChattingOneSameChannel"
    private val numberOfUsers = 6


    private val sessions: List<StatefulSession> = statefulSessions(channel, numberOfUsers)

    override fun flush() {
        //Do nothing because they are already initialized
    }


    private val allConnected = object : TestCase("assert all users get connected", this) {

        override fun test(): String {
            val allConnected =
                sessions.all { it.state.store.all { storeValue -> storeValue == "connected!" } }
            return if (allConnected)
                "OK"
            else
                "NOK" + sessions.map { it.states }
        }
    }

    private val oneMemberSendsMessage =
        object : TestCase("assert one member sends message and all others receive it", this) {

            override fun test(): String {
                chatApi.postMessage(channel, "user-test1-1", "message")
                Thread.sleep(500)


                val find = sessions.find { it.state.store.size != 2 }
                return if (find != null)
                    "OK"
                else
                    "NOK" + sessions.map { it.states }
            }
        }


    override fun close() {
        sessions.forEach { it.websocket.cancel() }
    }

    init {
        addTestCase(allConnected)
        addTestCase(oneMemberSendsMessage)
    }
}