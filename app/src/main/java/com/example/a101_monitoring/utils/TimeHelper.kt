package com.example.a101_monitoring.utils

import android.util.Log
import com.instacart.library.truetime.TrueTime
import com.instacart.library.truetime.TrueTimeRx
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TimeHelper {

    private val trueTimeMutex = Mutex()

    fun getTimeInMilliSeconds(): Long {
        if (isTrueTimeInitialized()) {
            return getCurrentTimeFromTrueTime()
        }
        return System.currentTimeMillis()
    }

    private fun fromSecToMillis(sec: Long) = sec * 1000

    fun initializeTimer() {
        object : Thread() {
            override fun run() {
                super.run()
                synchronized(trueTimeMutex) {
                    ExceptionsHelper.tryBlock(TAG, "initialize true time") {
                        if (!isTrueTimeInitialized()) {
                            initializeTrueTime(TRUE_TIME_INIT_MAX_TRIES)
                        }
                    }
                }
            }
        }.start()
    }

    private fun isTrueTimeInitialized() = TrueTime.isInitialized()

    private fun getCurrentTimeFromTrueTime() = TrueTime.now().time

    private fun initializeTrueTime(tries: Int) {
        if (tries > 0 && !isTrueTimeInitialized()) {
            TrueTime.build().initialize()
            if (!isTrueTimeInitialized()) {
                Log.i(TAG, "problem with initializing true time, remaining tries ${tries - 1}")
                initializeTrueTime(tries)
            } else {
                Log.i(TAG, "true time was initialized successfully")
            }

//            TrueTimeRx.build()
//                .initializeRx("time.google.com")
//                .subscribeOn(Schedulers.io())
//                .subscribe({
//                    DefaultCallbacksHelper.onSuccessDefault(
//                        TAG,
//                        "TrueTime was initialized and we have a time: $it"
//                    )
//                }, {
//                    DefaultCallbacksHelper.onErrorDefault(
//                        TAG,
//                        "error with initializing true time, remaining tries: ${tries - 1}",
//                        it
//                    )
//                    initializeTrueTime(tries - 1)
//                })
        }
    }


    fun executeWithConstantDelaySequentiallyInBackground(delaySeconds: Long, vararg blocks: () -> Unit) {
        object : Thread() {
            override fun run() {
                super.run()
                blocks.slice(0 until (blocks.size - 1)).forEach {
                    it()
                    Thread.sleep(fromSecToMillis(delaySeconds))
                }
                blocks.last()()
            }
        }.start()
    }

    companion object {
        const val TAG = "TimeHelper"
        val instance = TimeHelper()
        private const val TRUE_TIME_INIT_MAX_TRIES = 100
    }
}