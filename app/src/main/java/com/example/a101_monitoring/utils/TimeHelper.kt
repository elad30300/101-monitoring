package com.example.a101_monitoring.utils

import java.time.Duration
import kotlin.concurrent.thread

class TimeHelper {

    fun getTimeInMilliSeconds() = System.currentTimeMillis()

    fun fromSecToMillis(sec: Long) = sec * 1000

    fun executeWithConstantDelaySequentiallyInBackground(delaySeconds: Long, vararg blocks: () -> Unit) {
        Thread().run {
            blocks.slice(0 until (blocks.size - 1)).forEach {
                it()
                Thread.sleep(fromSecToMillis(delaySeconds))
            }
            blocks.last()()
        }
    }

    companion object {
        val instance = TimeHelper()
    }
}