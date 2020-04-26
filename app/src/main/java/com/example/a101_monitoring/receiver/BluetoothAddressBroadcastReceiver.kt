package com.example.a101_monitoring.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothAddressBroadcastReceiver : BroadcastReceiver() {

    var listener: BluetoothAddressReceiverListener? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.also {
            if (it.action == ACTION_BLUETOOTH_ADDRESS_SUBMITTED) {
                listener?.onBtAddressSubmitted(it.getStringExtra(EXTRA_ADDRESS))
            }
        }
    }

    interface BluetoothAddressReceiverListener {
        fun onBtAddressSubmitted(address: String)
    }

    companion object {
        const val ACTION_BLUETOOTH_ADDRESS_SUBMITTED = "com.example.a101_monitoring.receiver.ACTION_BLUETOOTH_ADDRESS_SUBMITTED"
        const val EXTRA_ADDRESS = "address"
    }

}