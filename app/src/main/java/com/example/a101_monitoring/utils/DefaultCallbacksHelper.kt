package com.example.a101_monitoring.utils

import android.util.Log
import com.example.a101_monitoring.bluetooth.BluetoothController

class DefaultCallbacksHelper {

    companion object {

        fun onErrorDefault(tag: String, message: String, throwable: Throwable? = null) {
            Log.e(tag, "$message, exception: ${throwable?.message}")
            throwable?.printStackTrace()
        }

        fun onSuccessDefault(tag: String, message: String) {
            Log.i(tag, message)
        }

    }

}