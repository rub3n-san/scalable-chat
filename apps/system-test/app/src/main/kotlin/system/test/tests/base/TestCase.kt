package system.test.tests.base

abstract class TestCase(val testName: String, val testSuite: TestSuite) {
    abstract fun test(): String

    fun doTest() {
        val startTime = System.currentTimeMillis()
        val result = test()

        val endTime = System.currentTimeMillis()
        val elapsedTime = endTime - startTime
        println("$result - ${testSuite.suiteName}::$testName executed in: $elapsedTime milliseconds")
    }


}