/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package system.test

import system.test.tests.Tests


class App {
    val greeting: String
        get() {
            return "System tests!"
        }
}

fun main() {
    println(App().greeting)

    Tests().suite()
    //Tests().flush()
}

