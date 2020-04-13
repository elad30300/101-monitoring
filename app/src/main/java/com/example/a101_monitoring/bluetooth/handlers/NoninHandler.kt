package com.example.a101_monitoring.bluetooth.handlers

import android.util.Log
import com.example.a101_monitoring.utils.DefaultCallbacksHelper
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import java.util.*

typealias CharacteristicNotificationCallback = (ByteArray) -> Unit
typealias ErrorCallback = (Throwable) -> Unit

class NoninHandler(
    device: RxBleDevice
) : BleDeviceHandler(device) {

    companion object {
        const val TAG = "NoninHandler"
    }

    override fun onConnected() {
        setupNotifications()
    }

    override fun onConnected(connection: RxBleConnection) {
        super.onConnected(connection)
        setupNotifications()
    }

    private fun setupNotifications() {
        setupOximeteryMeasurementsCharacteristicNotifications()
        setupRespirationCharacteristicNotifications()
    }

    private fun setupNotificationsForCharacteristic(uuid: UUID,
                                                    notificationCallback: CharacteristicNotificationCallback,
                                                    errorCallback: ErrorCallback) {
        getConnection()?.setupNotification(uuid)!!
            .doOnNext {
                it
            }
            .flatMap {
                it
            }
            .subscribe({
                notificationCallback(it)
            }, {
                errorCallback(it)
            })
    }

    private fun setupOximeteryMeasurementsCharacteristicNotifications() {
        val uuid = UUID.fromString(NoninGattAttributes.OximeteryService.Characteritics.NONIN_OXIMTERY_MEASURMENT)
        setupNotificationsForCharacteristic(
            uuid, {
                handleOximteryCharacteristicNotification(it)
            }, {
                DefaultCallbacksHelper.onErrorDefault(TAG, "Setup nonin oximetry characteristic notifications failed", it)
            }
        )
    }

    private fun setupRespirationCharacteristicNotifications() {
        val uuid = UUID.fromString(NoninGattAttributes.OximeteryService.Characteritics.NONIN_RESPIRATION_RATE_MEASURMENT)
        setupNotificationsForCharacteristic(
            uuid, {
                handleRespirationCharacteristicNotification(it)
            }, {
                DefaultCallbacksHelper.onErrorDefault(TAG, "Setup nonin respiration characteristic notifications failed", it)
            }
        )
    }

    private fun handleOximteryCharacteristicNotification(bytes: ByteArray) {
        val saturation = bytes[7].toInt() and 0xff
        val pulseRate =  ((bytes[8].toInt() and 0xff ) shl 8) or ((bytes[9].toInt()) and 0xff)
        Log.i(TAG, "nonin oximetry message, saturation: ${if (isSaturationValueMissing(saturation)) "missing" else saturation}, hr: ${if (isHeartRateValueMissing(pulseRate)) "missing" else pulseRate}")
    }

    private fun handleRespirationCharacteristicNotification(bytes: ByteArray) {
        val respiratoryRate = bytes[4].toInt() and 0xff
        Log.i(TAG, "nonin respiratory rate message, respiratory: ${if (isRespiratoryRateValueMissing(respiratoryRate)) "missing" else respiratoryRate}")
    }

    private fun isHeartRateValueMissing(value: Int) = isMeasurmentMissing(value, NoninGattAttributes.OximeteryService.Characteritics.HEART_RATE_MISSING_VALUE)
    private fun isSaturationValueMissing(value: Int) = isMeasurmentMissing(value, NoninGattAttributes.OximeteryService.Characteritics.SATURATION_MISSING_VALUE)
    private fun isRespiratoryRateValueMissing(value: Int) = isMeasurmentMissing(value, NoninGattAttributes.OximeteryService.Characteritics.RESPIRATORY_RATE_MISSING_VALUE)
    private fun isMeasurmentMissing(value: Int, missingValue: Int) = value == missingValue

    private object NoninGattAttributes {

        object OximeteryService {
            const val NONIN_OXIMTERY_SERVICE = "46A970E0-0D5F-11E2-8B5E-0002A5D5C51B"

            object Characteritics {
                const val NONIN_OXIMTERY_MEASURMENT = "0AAD7EA0-0D60-11E2-8E3C-0002A5D5C51B"
                const val NONIN_RESPIRATION_RATE_MEASURMENT = "EC0A8F24-4D24-11E7-B114-B2F933D5FE66"
                const val HEART_RATE_MISSING_VALUE = 511
                const val SATURATION_MISSING_VALUE = 127
                const val RESPIRATORY_RATE_MISSING_VALUE = 127
            }
        }
    }

}