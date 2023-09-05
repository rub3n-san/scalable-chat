package system.test.tests

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import system.test.integration.ChatApiClient
import system.test.integration.WebSocketClient
import system.test.integration.service.Chat

class Tests {

    private val chatApi = ChatApiClient("http://localhost:8080")
    private val chat = Chat(chatApi, WebSocketClient())

    private val suites = listOf(
        Basic(chatApi, chat),
        ContinuousChattingOneSameChannel(chatApi, chat),
        ContinuousChattingOnMultipleChannels(chatApi, chat)
    )

    fun suite() {
        runBlocking {
            suites.forEach { launch { it.runSuite() } }
        }
    }

    fun flush() {
        runBlocking {
            suites.forEach { launch { it.flush() } }
        }
    }


}