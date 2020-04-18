package com.example.a101_monitoring.utils

import com.instacart.library.truetime.TrueTimeRx
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TimeHelper {

    private val trueTimeMutex = Mutex()

    fun getTimeInMilliSeconds(): Long {
        if (TrueTimeRx.isInitialized()) {
            return TrueTimeRx.now().time
        }
        return System.currentTimeMillis()
    }

    fun fromSecToMillis(sec: Long) = sec * 1000

    fun initializeTimer() {
        Thread().run {
            runBlocking {
                trueTimeMutex.withLock {
                    if (!TrueTimeRx.isInitialized()) {
                        initializeTrueTime(TRUE_TIME_INIT_MAX_TRIES)
                    }
                }
            }
        }
    }

    private fun initializeTrueTime(tries: Int) {
        if (tries > 0 && !TrueTimeRx.isInitialized()) {
            TrueTimeRx.build()
                .initializeRx("time.google.com")
                .subscribeOn(Schedulers.io())
                .subscribe({
                    DefaultCallbacksHelper.onSuccessDefault(
                        TAG,
                        "TrueTime was initialized and we have a time: $it"
                    )
                }, {
                    DefaultCallbacksHelper.onErrorDefault(
                        TAG,
                        "error with initializing true time, remaining tries: ${tries - 1}",
                        it
                    )
                    initializeTrueTime(tries - 1)
                })
        }
    }


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
        const val TAG = "TimeHelper"
        val instance = TimeHelper()
        private const val TRUE_TIME_INIT_MAX_TRIES = 100
    }
}