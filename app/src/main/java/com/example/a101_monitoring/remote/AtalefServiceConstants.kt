package com.example.a101_monitoring.remote

object AtalefServiceConstants {

    private val environment = Environment.PRODUCT

    private val baseUrlMap = mapOf(
        Environment.LOCAL to "http://192.168.43.230:8000/api/",
        Environment.DEVELOP to "https://atalefdev-server.azurewebsites.net/api/",
        Environment.TEST to "https://ataleftest-server.azurewebsites.net/api/",
        Environment.PRODUCT to "https://atalefprod-server.azurewebsites.net/api/"
    )

    val baseUrl = baseUrlMap[environment]

    enum class Environment {
        LOCAL,
        DEVELOP,
        TEST,
        PRODUCT
    }
}