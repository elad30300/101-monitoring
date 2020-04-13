package com.example.a101_monitoring.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.a101_monitoring.MyApplication
import com.example.a101_monitoring.bluetooth.BluetoothController
import com.example.a101_monitoring.utils.BroadcastConstants
import javax.inject.Inject

class SensorAddressUpdatedBroadcastReceiver(context: Context) : BroadcastReceiver() {

    @Inject lateinit var bluetoothController: BluetoothController

    init {
        (context.applicationContext as MyApplication).applicationComponent.inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.action?.let {
            if (it == BroadcastConstants.BroadcastActions.SENSOR_ADDRESS_UPDATED) {
                bluetoothController.scan(intent!!.getStringExtra(BroadcastConstants.BroadcastArgsNames.SENSOR_ADDRESS))
            }
        }
    }

}