package system.test.tests.base

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import system.test.integration.ChatApiClient
import system.test.integration.StatefulSession
import system.test.integration.service.Chat

abstract class TestSuite(open val suiteName: String, open val chatApi: ChatApiClient, open val chat: Chat) {
    private val testCases = mutableListOf<TestCase>()

    fun addTestCase(testCase: TestCase) {
        testCases.add(testCase)
    }

    fun runSuite() {
        println("$suiteName Test Suite $suiteName Running...")
        for (testCase in testCases) {
            testCase.doTest()
        }
        close()
        println("$suiteName Test Suite $suiteName Finished")
    }

    open fun flush() {}
    open fun close() {}

    fun statefulSessions(
        channel: String,
        numUsers: Int,
        userNameTemplate: String = "user-test1-"
    ): List<StatefulSession> {
        val sessions = runBlocking {
            val asyncResult = async {
                (1..numUsers).map {
                    val user = "$userNameTemplate$it"
                    chat.connect(channel, user)
                }
            }

            asyncResult.await()
        }

        Thread.sleep(500)
        return sessions
    }
}