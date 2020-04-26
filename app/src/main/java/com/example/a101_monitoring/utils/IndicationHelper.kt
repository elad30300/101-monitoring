package com.example.a101_monitoring.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object IndicationHelper {

    fun vibrate(context: Context) {
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val mVibratePattern = longArrayOf(65, 180, 65, 180, 65, 180)

        // Vibrate for newer APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(mVibratePattern, -1))
        } else {
            // Deprecated in API 26
            v.vibrate(500)
        }
    }
}