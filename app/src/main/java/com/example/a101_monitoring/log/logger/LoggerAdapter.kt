package com.example.a101_monitoring.log.logger

import android.util.Log
import java.util.concurrent.Executors

abstract class LoggerAdapter {
    private val executor = Executors.newSingleThreadExecutor()

    fun d(tag: String, msg: String) {
        executor.execute {
            logD(tag, msg)
            Log.d(tag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        executor.execute {
            logE(tag, msg)
            Log.e(tag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        executor.execute {
            logI(tag, msg)
            Log.i(tag, msg)
        }
    }

    fun v(tag: String, msg: String) {
        executor.execute {
            logV(tag, msg)
            Log.v(tag, msg)
        }
    }

    fun w(tag: String, msg: String) {
        executor.execute {
            logW(tag, msg)
            Log.w(tag, msg)
        }
    }

    protected abstract fun logD(tag: String, msg: String)
    protected abstract fun logE(tag: String, msg: String)
    protected abstract fun logI(tag: String, msg: String)
    protected abstract fun logV(tag: String, msg: String)
    protected abstract fun logW(tag: String, msg: String)
}