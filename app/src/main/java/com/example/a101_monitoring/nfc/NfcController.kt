package com.example.a101_monitoring.nfc

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.nfc.NdefMessage
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.a101_monitoring.receiver.BluetoothAddressBroadcastReceiver
import com.example.a101_monitoring.utils.DefaultCallbacksHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NfcController @Inject constructor(private val localBroadcastManager: LocalBroadcastManager) {

    fun onNdefTagScanned(messages: List<NdefMessage>) {
        if (onOptionalBtAddressScanned(messages)) {
            return
        }
    }

    private fun onOptionalBtAddressScanned(messages: List<NdefMessage>): Boolean {
        try {
            val payload = messages[0].records[0].payload
            val data = String(payload.sliceArray(3 .. payload.lastIndex))
            if (BluetoothAdapter.checkBluetoothAddress(data)) {
                Log.i(TAG, "bt address was scanned $data")
                onBtAddressScanned(data)
                return true
            }
        } catch (exception: Exception) {
            DefaultCallbacksHelper.onErrorDefault(TAG, "exception in trying to check for bt address in nfc tag", exception)
        } finally {
            return false
        }
    }

    private fun onBtAddressScanned(address: String) {
        Intent(Intent.ACTION_ATTACH_DATA).apply {
            action = BluetoothAddressBroadcastReceiver.ACTION_BLUETOOTH_ADDRESS_SUBMITTED
            putExtra(BluetoothAddressBroadcastReceiver.EXTRA_ADDRESS, address)
            if (localBroadcastManager.sendBroadcast(this)) {
                Log.i(TAG, "sent bt address broadcast successfully")
            } else {
                Log.e(TAG, "failed send bt address broadcast")
            }
        }
    }

    companion object {
        const val TAG = "NfcController"
    }

}