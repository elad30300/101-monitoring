package com.example.a101_monitoring.utils

import android.util.Log
import com.example.a101_monitoring.bluetooth.BluetoothController
import com.example.a101_monitoring.log.logger.Logger

class DefaultCallbacksHelper {

    companion object {

        fun onErrorDefault(tag: String, message: String, throwable: Throwable? = null, logger: Logger? = null) {
            val logMessage = "$message, exception: ${throwable?.message}"
            logger?.e(tag, logMessage) ?: Log.e(tag, logMessage)
            throwable?.printStackTrace()
        }

        fun onSuccessDefault(tag: String, message: String, logger: Logger? = null) {
            logger?.i(tag, message) ?: Log.i(tag, message)
        }

    }

}