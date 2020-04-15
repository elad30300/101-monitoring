package com.example.a101_monitoring.remote

object AtalefServiceConstants {

    private val environment = Environment.TEST

    private val baseUrlMap = mapOf(
        Environment.DEVELOP to "https://atalefdev-server.azurewebsites.net/api/",
        Environment.TEST to "https://ataleftest-server.azurewebsites.net/api/",
        Environment.PRODUCT to "https://atalefprod-server.azurewebsites.net/api/"
    )

    val baseUrl = baseUrlMap[environment]

    enum class Environment {
        DEVELOP,
        TEST,
        PRODUCT
    }
}