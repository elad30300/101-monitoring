package com.example.a101_monitoring.log.logger

import android.util.Log
import java.util.concurrent.Executors

abstract class LoggerAdapter {
    private val executor = Executors.newSingleThreadExecutor()

    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
        executor.execute {
            logD(tag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        Log.e(tag, msg)
        executor.execute {
            logE(tag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
        executor.execute {
            logI(tag, msg)
        }
    }

    fun v(tag: String, msg: String) {
        Log.v(tag, msg)
        executor.execute {
            logV(tag, msg)
        }
    }

    fun w(tag: String, msg: String) {
        Log.w(tag, msg)
        executor.execute {
            logW(tag, msg)
        }
    }

    protected abstract fun logD(tag: String, msg: String)
    protected abstract fun logE(tag: String, msg: String)
    protected abstract fun logI(tag: String, msg: String)
    protected abstract fun logV(tag: String, msg: String)
    protected abstract fun logW(tag: String, msg: String)
}