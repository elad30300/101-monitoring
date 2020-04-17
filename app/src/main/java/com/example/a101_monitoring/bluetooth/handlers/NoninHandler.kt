package com.example.a101_monitoring.bluetooth.handlers

import android.content.Context
import android.util.Log
import com.example.a101_monitoring.MyApplication
import com.example.a101_monitoring.data.model.HeartRate
import com.example.a101_monitoring.data.model.RespiratoryRate
import com.example.a101_monitoring.data.model.Saturation
import com.example.a101_monitoring.repository.MeasurementsRepository
import com.example.a101_monitoring.repository.PatientRepository
import com.example.a101_monitoring.utils.DefaultCallbacksHelper
import com.example.a101_monitoring.utils.TimeHelper
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.disposables.Disposable
import java.lang.Exception
import java.util.*
import javax.inject.Inject

typealias CharacteristicNotificationCallback = (ByteArray) -> Unit
typealias ErrorCallback = (Throwable) -> Unit

class NoninHandler(
    private val context: Context,
    device: RxBleDevice
) : BleDeviceHandler(device) {

    private var oximeteryCharacteristicNotificationsDisposable: Disposable? = null
    private var lastRespiratoryRate = NoninGattAttributes.OximeteryService.Characteritics.RESPIRATORY_RATE_MISSING_VALUE

    @Inject lateinit var patientRepository: PatientRepository
    @Inject lateinit var measurementsRepository: MeasurementsRepository

    init {
        initializeDependencies()
    }

    private fun initializeDependencies() {
        (context.applicationContext as MyApplication).applicationComponent.inject(this)
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
                                                    errorCallback: ErrorCallback): Disposable {
        return getConnection()?.setupNotification(uuid)!!
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
        oximeteryCharacteristicNotificationsDisposable = setupNotificationsForCharacteristic(
            uuid, {
                handleOximteryCharacteristicNotification(it)
            }, {
                DefaultCallbacksHelper.onErrorDefault(TAG, "Setup nonin oximetry characteristic notifications failed", it)
            }
        )
    }

    private fun disableOximeteryMeasurementsCharacteristicNotifications() {
        oximeteryCharacteristicNotificationsDisposable?.dispose()
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
        val heartRate =  ((bytes[8].toInt() and 0xff ) shl 8) or ((bytes[9].toInt()) and 0xff)
        Log.i(TAG, "nonin oximetry message, saturation: ${if (isSaturationValueMissing(saturation)) "missing" else saturation}, hr: ${if (isHeartRateValueMissing(heartRate)) "missing" else heartRate}")
        onOximeteryMeasurements(heartRate, saturation)
    }

    private fun handleRespirationCharacteristicNotification(bytes: ByteArray) {
        val respiratoryRate = bytes[4].toInt() and 0xff
        lastRespiratoryRate = respiratoryRate
        Log.i(TAG, "nonin respiratory rate message, respiratory: ${if (isRespiratoryRateValueMissing(respiratoryRate)) "missing" else respiratoryRate}")
    }

    private fun onOximeteryMeasurements(heartRateValue: Int, saturationValue: Int) {
        try {
            val time = TimeHelper.instance.getTimeInMilliSeconds()
            val patientId = patientRepository.getPatientIdBySensorAddress(getDevice().macAddress)
            measurementsRepository.insertMeasurements(
                if (isHeartRateValueMissing(heartRateValue)) null else HeartRate(heartRateValue, time, patientId),
                if (isSaturationValueMissing(saturationValue)) null else Saturation(saturationValue, time, patientId),
                if (isRespiratoryRateValueMissing(lastRespiratoryRate)) null else RespiratoryRate(lastRespiratoryRate, time, patientId)
            )
            pauseMeasurements(SAMPLE_RATE_SEC.toLong())
        } catch (exception: Exception) {
            DefaultCallbacksHelper.onErrorDefault(TAG, "Couldn't handle measurements after parsing", exception)
        }
    }

    private fun pauseMeasurements(duration: Long) {
        TimeHelper.instance.executeWithConstantDelaySequentiallyInBackground(duration,
            {
                disableOximeteryMeasurementsCharacteristicNotifications()
            }, {
                setupOximeteryMeasurementsCharacteristicNotifications()
            }
        )
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

    companion object {
        const val TAG = "NoninHandler"
        const val SAMPLE_RATE_SEC = 60
    }

}