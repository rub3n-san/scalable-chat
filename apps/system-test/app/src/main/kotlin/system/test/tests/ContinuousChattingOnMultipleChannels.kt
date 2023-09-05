package system.test.tests

import system.test.integration.ChatApiClient
import system.test.integration.StatefulSession
import system.test.integration.service.Chat
import system.test.tests.base.TestCase
import system.test.tests.base.TestSuite

class ContinuousChattingOnMultipleChannels(override val chatApi: ChatApiClient, override val chat: Chat) :
    TestSuite("ContinuousChattingOnMultipleChannels", chatApi, chat) {

    val channel1 = "ContinuousChattingOnMultipleChannels1"
    val channel2 = "ContinuousChattingOnMultipleChannels2"
    private val sessionsChanel1: List<StatefulSession>
    private val sessionsChanel2: List<StatefulSession>
    private val numberOfUsers = 3

    init {
        sessionsChanel1 = statefulSessions(channel1, numberOfUsers)
        sessionsChanel2 = statefulSessions(channel2, numberOfUsers)
        Thread.sleep(500)
    }

    override fun flush() {
        //Do nothing because they are already initialized
    }

    private val allConnected = object : TestCase("assert all users get connected", this) {

        override fun test(): String {
            val allConnectedChannel1 =
                sessionsChanel1.all { it.state.store.all { storeValue -> storeValue == "connected!" } }
            val allConnectedChannel2 =
                sessionsChanel2.all { it.state.store.all { storeValue -> storeValue == "connected!" } }
            return if (allConnectedChannel1 && allConnectedChannel2) {
                "OK"
            } else {
                "NOK" + " session1" + sessionsChanel1.map { it.states } + " session2" + sessionsChanel2.map { it.states }
            }

        }
    }

    private val oneMemberSendsMessageToChannel1 =
        object : TestCase("assert only the channel members receive the message (except himself)", this) {

            override fun test(): String {
                chatApi.postMessage(channel1, "user-test1-1", "message")
                Thread.sleep(500)


                val find = sessionsChanel1.find { it.state.store.size != 2 }
                val find2 = sessionsChanel2.find { it.state.store.contains("connected!").not() }
                return if (find != null && find2 == null)
                    "OK"
                else
                    "NOK" + " session1" + sessionsChanel1.map { it.states } + " session2" + sessionsChanel2.map { it.states }
            }
        }


    override fun close() {
        sessionsChanel1.forEach { it.websocket.cancel() }
        sessionsChanel2.forEach { it.websocket.cancel() }
    }

    init {
        addTestCase(allConnected)
        addTestCase(oneMemberSendsMessageToChannel1)
    }
}