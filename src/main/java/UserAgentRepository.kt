package com.example.whattoeat.data.net.hack

import java.nio.file.Files
import java.nio.file.Paths

class UserAgentRepository() {


    private val list = Files.readAllLines(Paths.get("src/main/resources/user_agents.txt"))

    private val usedUa = mutableMapOf<Long, String>()

    fun getRandomUserAgent(notUsed: Boolean = true): String {
        if (notUsed)
            getRandomNotUsedUserAgent()

        val randomUa = list.random()

        println("Picked user-agent: $randomUa")

        usedUa.put(System.nanoTime(), randomUa)

        println("Returned user-agent: $randomUa")

        return randomUa
    }


    private fun getRandomNotUsedUserAgent(): String {
        val randomUa = list.random()

        println("Picked user-agent: $randomUa")

        if (usedUa.values.contains(randomUa)) {
            println("User-agent was using")

            sizeHandling()
            getRandomNotUsedUserAgent()
        }

        println("Returned user-agent: $randomUa")
        return randomUa
    }

    private fun sizeHandling() {
        val size = (usedUa.size * 1.5).toInt()

        println("Repeating $size times to remove used user-agents")

        if (size >= list.size)
            repeat(size) {
                usedUa.remove(usedUa.keys.sorted().random())
            }
    }

    companion object {
        private const val TAG = "TEST-TAG"
    }
}