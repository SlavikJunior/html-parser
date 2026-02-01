package com.example.whattoeat.data.net.hack

import ProxyManager
import java.net.InetSocketAddress
import java.net.Proxy
import java.nio.file.Files
import java.nio.file.Paths


class ProxyRepository {

    private val proxyManager = ProxyManager()

    private val proxyList = mutableListOf<Proxy>()

    private val usedProxy = mutableMapOf<Long, Proxy>()

    init {
        proxyManager.loadProxiesFromJson(Files.readAllLines(Paths.get("src/main/resources/proxylist.jdproxies")).joinToString("\n"));

        proxyList.addAll(proxyManager.allProxies)
        // Инициализируем и проверяем прокси
        println("Инициализация прокси...")

        println("Загружено ${proxyList.size} прокси")

    }

    fun printProxyStats() {
        println("\n=== Статистика прокси ===")
        println("Всего прокси: ${proxyList.size}")
        println("Использованных прокси: ${usedProxy.size}")
        println("Последние 5 использованных: ${usedProxy.values.toList().takeLast(5)}")
    }

    fun getRandomProxy(
        notUsed: Boolean = true
    ): Proxy {
        return if (notUsed) getRandomNotUsedProxy() else {
            val randomProxy = proxyList.random()
            println("Picked proxy: $randomProxy")
            usedProxy[System.currentTimeMillis()] = randomProxy
            randomProxy
        }
    }

    private fun getRandomNotUsedProxy(): Proxy {
        val unusedProxies = proxyList.filterNot { usedProxy.values.contains(it) }

        return if (unusedProxies.isNotEmpty()) {
            val proxy = unusedProxies.random()
            usedProxy[System.currentTimeMillis()] = proxy
            println("Выбран неиспользованный прокси: $proxy")
            proxy
        } else {
            // Все прокси использованы, очищаем список
            println("Все прокси использованы, очищаем историю...")
            usedProxy.clear()
            val proxy = proxyList.random()
            usedProxy[System.currentTimeMillis()] = proxy
            proxy
        }
    }
}