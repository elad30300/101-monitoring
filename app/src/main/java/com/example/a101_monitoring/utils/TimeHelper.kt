package com.example.a101_monitoring.utils

class TimeHelper {

    fun getTimeInMiliSeconds() = System.currentTimeMillis()

    companion object {
        val instance = TimeHelper()
    }
}