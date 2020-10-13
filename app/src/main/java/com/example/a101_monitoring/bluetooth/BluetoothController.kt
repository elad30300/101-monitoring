package com.example.a101_monitoring.bluetooth

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.a101_monitoring.bluetooth.handlers.NoninHandler
import com.example.a101_monitoring.data.model.*
import com.example.a101_monitoring.log.logger.Logger
import com.example.a101_monitoring.repository.MeasurementsRepository
import com.example.a101_monitoring.repository.PatientRepository
import com.example.a101_monitoring.utils.DefaultCallbacksHelper
import com.example.a101_monitoring.utils.TimeHelper
import com.polidea.rxandroidble2.scan.ScanSettings
import java.io.Closeable
import java.lang.Exception
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

@Singleton
class BluetoothController @Inject constructor(
    private val context: Application,
//    private val rxBleClient: RxBleClient,
    private val patientRepository: PatientRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val logger: Logger
) : Closeable, GattCallback.Delegate {

    @field:[Inject Named("BleExecutor")]
    lateinit var executor: Executor

    lateinit var sensors: LiveData<List<Sensor>>
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    //    private val bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
    private val bluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }
    private var isScanning = false
    private var handler = Handler()
    private val sharedResourcesLock = ReentrantLock()
    private val characteristicsLock = ReentrantLock()

    //    private val scanDisposables = mutableMapOf<String, Disposable>()
    private var currentScanCallback: ScanCallback? = null
    private var alreadyScheduledScans = false
    private var maintainScansTimer: Timer? = null
    private var currentScanThread: Thread? = null
    private val scanQueue = mutableListOf<String>()
    private val connectingDevices = mutableListOf<String>()
    private val connectedDevices = mutableListOf<BluetoothGatt>()

    private val lastRespiratoryRates = mutableMapOf<String, Int>()
    private val nextMissingValuesInsertInMillis = mutableMapOf<String, Long>()
//    private var lastRespiratoryRate =
//        NoninHandler.NoninGattAttributes.OximeteryService.Characteritics.RESPIRATORY_RATE_MISSING_VALUE // todo: move to handler as soon as possible

    private val scanSettings = android.bluetooth.le.ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private fun setIsScanning(isScanning: Boolean) {
//        synchronized(sharedResourcesLock) {
        this.isScanning = isScanning
//        }
    }

    private fun getIsScanning(): Boolean {
        var value: Boolean
//        synchronized(sharedResourcesLock) {
        value = isScanning
//        }
        return value
    }


    private fun continueScanQueue() {
        synchronized(sharedResourcesLock) {
            if (scanQueue.isNotEmpty()) {
                val address = scanQueue.removeAt(0)
                Log.d(TAG, "removed $address from scan queue")
            }
        }
    }

    private fun getCurrentScanAddress(): String? {
        val address: String?
        synchronized(sharedResourcesLock) {
            address = scanQueue.firstOrNull()
        }
        return address
    }

    private fun isDeviceInSensors(address: String): Boolean {
        return sensors?.value?.any { it.address == address } ?: false
    }

    private fun isDeviceConnected(address: String): Boolean {
        return connectedDevices.any { it.device.address == address }
    }

    private fun isDeviceConnecting(address: String): Boolean {
        return connectingDevices.any { it == address }
    }

    private fun generateScanCallback() = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult?) {
            super.onScanResult(callbackType, result)
            thread {
                synchronized(this@BluetoothController) {
                    if (Looper.getMainLooper().isCurrentThread) {
                        Log.e(TAG, "scan results on main thread!!!")
                    }
                    if (this != currentScanCallback) {
                        Log.d(
                            TAG,
                            "onScanResult - called by $this not current scan callback ${currentScanCallback}, doesn't continue"
                        )
                        return@synchronized
                    }
                    setIsScanning(false)
                    result?.device?.also { device ->
                        if (isDeviceInSensors(device.address)) {
                            if (!isDeviceConnected(device.address)) {
                                if (!isDeviceConnecting(device.address)) {
                                    logger.i(
                                        TAG,
                                        "found device $${device.address} that should be connected!"
                                    )
                                    connect(device)
                                } else {
                                    logger.d(
                                        TAG,
                                        "found connecting device ${device.address}, doesnt continue"
                                    )
                                }
                            } else {
                                logger.d(
                                    TAG,
                                    "found connected device ${device.address}, doesnt continue"
                                )
                            }
                        } else {
                            logger.d(
                                TAG,
                                "found device that is not in sensors ${device.address}, doesnt continue"
                            )
                        }
                    }
                }
//                //
//                if (isDeviceInConnectingList(device.address) || isDeviceInConnectedList(device.address) || getCurrentScanAddress() != device.address) {
//                    Log.d(
//                        TAG,
//                        "found result ${device.address} in scan of connected/connecting device or not the current scan address, not preceeding"
//                    )
//                    return@also
//                }
//                Log.i(
//                    TAG,
//                    "Found BLE device! Name: ${device.name ?: "Unnamed"}, address: ${device.address}"
//                )
//                connect(device)
//                synchronized(sharedResourcesLock) {
//                    scanQueue.removeIf { it == device.address }
//                }
//                maintainScans()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            stopScan()
//            continueScanQueue()
//            setIsScanning(false)
//            maintainScans()
            logger.e(TAG, "scan failed with code $errorCode")
        }
    }

//
//    private val scanCallback = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult?) {
//            super.onScanResult(callbackType, result)
//            setIsScanning(false)
//            result?.device?.also { device ->
//                if (isDeviceInConnectingList(device.address) || isDeviceInConnectedList(device.address) || getCurrentScanAddress() != device.address) {
//                    Log.d(
//                        TAG,
//                        "found result ${device.address} in scan of connected/connecting device or not the current scan address, not preceeding"
//                    )
//                    return@also
//                }
//                Log.i(
//                    TAG,
//                    "Found BLE device! Name: ${device.name ?: "Unnamed"}, address: ${device.address}"
//                )
//                connect(device)
//                synchronized(sharedResourcesLock) {
//                    scanQueue.removeIf { it == device.address }
//                }
//                maintainScans()
//            }
//        }
//
//        override fun onScanFailed(errorCode: Int) {
//            super.onScanFailed(errorCode)
//            stopScan()
//            continueScanQueue()
//            setIsScanning(false)
//            maintainScans()
//            Log.e(TAG, "scan failed with code $errorCode")
//        }
//    }

    private fun removeFromConnectingDevicesList(address: String) {
        if (connectingDevices.removeIf { it == address }) {
            logger.d(TAG, "removed device $address from connecting devices list")
            printConnectingDevices()
        }
    }

    private fun removeFromConnectedDevicesList(address: String) {
        if (connectedDevices.removeIf { it.device.address == address }) {
            logger.d(TAG, "removed device ${address} from connected devices list")
            printConnectingDevices()
        }
    }

    private fun addToConnectedDevices(gatt: BluetoothGatt) {
        connectedDevices.add(gatt)
    }

    override fun onConnected(gatt: BluetoothGatt?) {
        thread {
            synchronized(this) {
                gatt?.also {
                    if (!isDeviceConnected(it.device.address)) {
                        DefaultCallbacksHelper.onSuccessDefault(
                            TAG,
                            "Establish connection to device with address ${it.device.address} succeeded",
                            logger = logger
                        )
//                synchronized(sharedResourcesLock) {
                        synchronized(lastRespiratoryRates) {
                            lastRespiratoryRates[it.device.address] =
                                NoninHandler.NoninGattAttributes.OximeteryService.Characteritics.RESPIRATORY_RATE_MISSING_VALUE
                        }
                        synchronized(nextMissingValuesInsertInMillis) {
                            nextMissingValuesInsertInMillis[it.device.address] = Calendar.getInstance().timeInMillis
                        }
                        removeFromConnectingDevicesList(it.device.address)
                        addToConnectedDevices(it)
//                }
                        patientRepository.setSensorIsConnected(gatt.device.address, true)
                        discoverServices(it)
                    } else {
                        logger.d(
                            TAG,
                            "onConnected for already connected device ${it.device.address}, doesnt continue"
                        )
                    }
                    //
//            bleDeviceHandler.onConnected(connection)
//            observeConnectionState(bleDeviceHandler)
                }
            }
        }
    }

    private fun discoverServices(gatt: BluetoothGatt) {
//        executor.execute {
        thread {
            var count = 1000
            logger.d(
                TAG,
                "about to discover services with count $count for device ${gatt.device.address}"
            )
            while (count-- > 0 && !gatt.discoverServices()) {
                logger.d(
                    TAG,
                    "about to discover services with count $count for device ${gatt.device.address}"
                )
            }
        }
//        }
    }

    override fun onDisconnected(gatt: BluetoothGatt?) {
        thread {
            synchronized(this) {
                DefaultCallbacksHelper.onSuccessDefault(
                    TAG,
                    "Device with address ${gatt?.device?.address} disconnected",
                    logger = logger
                )

                gatt?.also {
                    //
//        val deviceHandler = findDeviceInConnectedDevices(bleDeviceHandler.getDevice().macAddress)
//
//        connectedDevicesList.remove(deviceHandler)
                    it.close()
//            synchronized(sharedResourcesLock) {
                    synchronized(lastRespiratoryRates) {
                        try {
                            lastRespiratoryRates.remove(it.device.address)
                        } catch (ex: Exception) {
                            DefaultCallbacksHelper.onErrorDefault(
                                TAG,
                                "failure in remove resp from last backup for ${it.device.address}"
                            )
                        }
                    }
                    synchronized(nextMissingValuesInsertInMillis) {
                        try {
                            nextMissingValuesInsertInMillis.remove(it.device.address)
                        } catch (ex: Exception) {
                            DefaultCallbacksHelper.onErrorDefault(
                                TAG,
                                "failure in remove resp from last backup for ${it.device.address}"
                            )
                        }
                    }
                    removeFromConnectingDevicesList(it.device.address)
                    removeFromConnectedDevicesList(it.device.address)
//            }
                    if (isDeviceInSensors(it.device.address)) {
                        patientRepository.setSensorIsConnected(it.device.address, false)
                    } else {
                        Log.d(TAG, "disconnected from device that is not in sensors")
                    }
                }
            }
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?) {
        logger.d(TAG, "on services discovered for device ${gatt?.device?.address}")
        gatt?.also {
            thread {
                Thread.sleep(100)
                writeToNoninOximteryDescriptor(it)
            }
            thread {
                Thread.sleep(100)
                writeToNoninRespirationDescriptor(it)
            }
            thread {
                Thread.sleep(250)
                setupNoninOximetryNotifications(it)
            }
            thread {
                Thread.sleep(200)
                setupNoninRespirationNotifications(it)
            }
        }
    }

    fun writeToNoninOximteryDescriptor(gatt: BluetoothGatt?) {
        synchronized(characteristicsLock) {
            gatt?.also {
                it.getService(UUID.fromString(NoninHandler.NoninGattAttributes.OximeteryService.NONIN_OXIMTERY_SERVICE))
                    .also { service ->
                        service.getCharacteristic(UUID.fromString(NoninHandler.NoninGattAttributes.OximeteryService.Characteritics.NONIN_OXIMTERY_MEASURMENT))
                            .also { char ->
                                char.getDescriptor(CLIENT_CONFIG_DESCRIPTOR_UUID)
                                    .also { descriptor ->
                                        var count = 1000
                                        logger.d(
                                            TAG,
                                            "try to write to descriptor notifications to nonin oxymetry with count $count"
                                        )
                                        descriptor.value =
                                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                        while (count-- > 0 && !gatt.writeDescriptor(descriptor)) {
                                            Thread.sleep(10)
                                            logger.d(
                                                TAG,
                                                "try to write to descriptor notifications to nonin oxymetry with count $count"
                                            )
                                        }
                                        if (count == 0) {
                                            logger.e(
                                                TAG,
                                                "write to descriptor notifications to nonin oxymetry failed"
                                            )
                                            // todo: disconnect and reconnect later
                                        }
                                    }
                            }
                    }
            }
        }
    }

    fun writeToNoninRespirationDescriptor(gatt: BluetoothGatt?) {
        synchronized(characteristicsLock) {
            gatt?.also {
                it.getService(UUID.fromString(NoninHandler.NoninGattAttributes.OximeteryService.NONIN_OXIMTERY_SERVICE))
                    .also { service ->
                        service.getCharacteristic(UUID.fromString(NoninHandler.NoninGattAttributes.OximeteryService.Characteritics.NONIN_RESPIRATION_RATE_MEASURMENT))
                            .also { char ->
                                char.getDescriptor(CLIENT_CONFIG_DESCRIPTOR_UUID)
                                    .also { descriptor ->
                                        var count = 1000
                                        logger.d(
                                            TAG,
                                            "try to write to descriptor notifications to nonin respiration with count $count"
                                        )
                                        descriptor.value =
                                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                        while (count-- > 0 && !gatt.writeDescriptor(descriptor)) {
                                            Thread.sleep(10)
                                            logger.d(
                                                TAG,
                                                "try to write to descriptor notifications to nonin respiration with count $count"
                                            )
                                        }
                                        if (count == 0) {
                                            logger.e(
                                                TAG,
                                                "write to descriptor notifications to nonin respiration failed"
                                            )
                                            // todo: disconnect and reconnect later
                                        }
                                    }
                            }
                    }
            }
        }
    }

    fun setupNoninOximetryNotifications(gatt: BluetoothGatt) {
        synchronized(characteristicsLock) {
            gatt.getService(UUID.fromString(NoninHandler.NoninGattAttributes.OximeteryService.NONIN_OXIMTERY_SERVICE))
                .also { service ->
                    service.getCharacteristic(UUID.fromString(NoninHandler.NoninGattAttributes.OximeteryService.Characteritics.NONIN_OXIMTERY_MEASURMENT))
                        .also { char ->
                            var count = 1000
                            logger.d(
                                TAG,
                                "device ${gatt?.device?.address} try to setup notifications to nonin oximetry with count $count"
                            )
                            while (count-- > 0 && !gatt.setCharacteristicNotification(char, true)) {
                                Thread.sleep(10)
                                logger.d(
                                    TAG,
                                    "device ${gatt?.device?.address} try to setup notifications to nonin oximetry with count $count"
                                )
                            }
                            if (count == 0) {
                                logger.e(
                                    TAG,
                                    "device ${gatt?.device?.address} setup notifications to nonin oximetry failed"
                                )
                                // todo: disconnect and reconnect later
                            }
                        }
                }
        }
    }

    private fun disableOximeteryMeasurementsCharacteristicNotifications(gatt: BluetoothGatt?) {
        synchronized(characteristicsLock) {
            gatt?.getService(UUID.fromString(NoninHandler.NoninGattAttributes.OximeteryService.NONIN_OXIMTERY_SERVICE))
                ?.also { service ->
                    service.getCharacteristic(UUID.fromString(NoninHandler.NoninGattAttributes.OximeteryService.Characteritics.NONIN_OXIMTERY_MEASURMENT))
                        .also { char ->
                            var count = 1000
                            logger.d(
                                TAG,
                                "device ${gatt?.device?.address} try to disable notifications from nonin oximetry with count $count"
                            )
                            synchronized(sharedResourcesLock) {
                                while (count-- > 0 && !gatt.setCharacteristicNotification(
                                        char,
                                        false
                                    )
                                ) {
                                    Thread.sleep(10)
                                    logger.d(
                                        TAG,
                                        "device ${gatt?.device?.address} try to disable notifications from nonin oximetry with count $count"
                                    )
                                }
                            }
                            if (count == 0) {
                                logger.e(
                                    TAG,
                                    "device ${gatt?.device?.address} disable notifications to nonin oxymetry failed"
                                )
                                // todo: disconnect and reconnect later
                            }
                        }
                }
        }
    }

    fun setupNoninRespirationNotifications(gatt: BluetoothGatt) {
        synchronized(characteristicsLock) {
            gatt.getService(UUID.fromString(NoninHandler.NoninGattAttributes.OximeteryService.NONIN_OXIMTERY_SERVICE))
                .also { service ->
                    service.getCharacteristic(UUID.fromString(NoninHandler.NoninGattAttributes.OximeteryService.Characteritics.NONIN_RESPIRATION_RATE_MEASURMENT))
                        .also { char ->
                            var count = 1000
                            logger.d(
                                TAG,
                                "device ${gatt?.device?.address} try to setup notifications to nonin respiration with count $count"
                            )
                            while (count-- > 0 && !gatt.setCharacteristicNotification(char, true)) {
                                Thread.sleep(10)
                                logger.d(
                                    TAG,
                                    "device ${gatt?.device?.address} try to setup notifications to nonin respiration with count $count"
                                )
                            }
                            if (count == 0) {
                                logger.e(
                                    TAG,
                                    "device ${gatt?.device?.address} setup notifications to nonin respiration failed"
                                )
                                // todo: disconnect and reconnect later
                            }
                        }
                }
        }
    }

    private fun pauseMeasurements(gatt: BluetoothGatt?, duration: Long) {
        gatt?.also {
            TimeHelper.instance.executeWithConstantDelaySequentiallyInBackground(duration,
                {
                    disableOximeteryMeasurementsCharacteristicNotifications(it)
                }, {
                    setupNoninOximetryNotifications(it)
                }
            )
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        Log.d(
            TAG,
            "on characteristic changed device ${gatt?.device?.address} characteristic ${characteristic?.uuid}"
        )
        when (characteristic?.uuid) {
            UUID.fromString(NoninHandler.NoninGattAttributes.OximeteryService.Characteritics.NONIN_OXIMTERY_MEASURMENT) -> onOximeteryNotification(
                gatt,
                characteristic?.value
            )
            UUID.fromString(NoninHandler.NoninGattAttributes.OximeteryService.Characteritics.NONIN_RESPIRATION_RATE_MEASURMENT) -> onRespirationNotification(
                gatt,
                characteristic?.value
            )
        }
    }

    private fun onOximeteryNotification(gatt: BluetoothGatt?, value: ByteArray?) {
        value?.also { bytes ->
            val saturation = bytes[7].toInt() and 0xff
            val heartRate = ((bytes[8].toInt() and 0xff) shl 8) or ((bytes[9].toInt()) and 0xff)
            logger.i(
                NoninHandler.TAG,
                "device ${gatt?.device?.address} - nonin oximetry message, saturation: ${if (isSaturationValueMissing(
                        saturation
                    )
                ) "missing" else saturation}, hr: ${if (isHeartRateValueMissing(
                        heartRate
                    )
                ) "missing" else heartRate}"
            )
            onOximeteryMeasurements(gatt, heartRate, saturation)
        }
    }

    private fun getDeviceLastRespiratory(address: String): Int? = lastRespiratoryRates[address]

    private fun getDeviceNextMissingValuesInsertInMillis(address: String): Long? = nextMissingValuesInsertInMillis[address]

    private fun onOximeteryMeasurements(
        gatt: BluetoothGatt?,
        heartRateValue: Int,
        saturationValue: Int
    ) {
        gatt?.also {
            try {
                val time = TimeHelper.instance.getTimeInMilliSeconds()
                val patientId = patientRepository.getPatientIdBySensorAddress(it.device.address)
                val lastRespiratoryRate: Int
                synchronized(lastRespiratoryRates) {
                    lastRespiratoryRate = getDeviceLastRespiratory(it.device.address)
                        ?: NoninHandler.NoninGattAttributes.OximeteryService.Characteritics.RESPIRATORY_RATE_MISSING_VALUE
                }
                Log.d(TAG, "last respiration for ${it.device.address} is $lastRespiratoryRate")
                var allMissingValues =
                    isHeartRateValueMissing(heartRateValue) && isSaturationValueMissing(
                        saturationValue
                    ) && isRespiratoryRateValueMissing(lastRespiratoryRate)
                var shouldInsert = !allMissingValues
                val insertTime: Long
                synchronized(nextMissingValuesInsertInMillis) {
                    insertTime = getDeviceNextMissingValuesInsertInMillis(it.device.address) ?: Calendar.getInstance().timeInMillis
                }
                if (Calendar.getInstance().timeInMillis >= insertTime) {
                    shouldInsert = true
                }
                if (shouldInsert) {
                    Log.d(TAG, "about to insert measurements for ${it.device.address}")
                    measurementsRepository.insertMeasurements(
                        HeartRate(
                            if (isHeartRateValueMissing(heartRateValue)) Measurement.MISSING_VALUE else heartRateValue,
                            time,
                            patientId
                        ),
                        Saturation(
                            if (isSaturationValueMissing(saturationValue)) Measurement.MISSING_VALUE else saturationValue,
                            time,
                            patientId
                        ),
                        RespiratoryRate(
                            if (isRespiratoryRateValueMissing(lastRespiratoryRate)) Measurement.MISSING_VALUE else lastRespiratoryRate,
                            time,
                            patientId
                        )
                    )
                    val nextTimeDate = Calendar.getInstance().time.apply {
                        ++minutes
                    }
                    synchronized(nextMissingValuesInsertInMillis) {
                        nextMissingValuesInsertInMillis[it.device.address] = nextTimeDate.time
                    }
                    if (allMissingValues) {
                        logger.i(
                            NoninHandler.TAG,
                            "all measurements missing for device ${it.device.address}"
                        )
                    } else {
                        pauseMeasurements(gatt, NoninHandler.SAMPLE_RATE_SEC.toLong())
                    }
                }
//                if (allMissingValues) {
//                    logger.i(
//                        NoninHandler.TAG,
//                        "all measurements missing for device ${it.device.address}"
//                    )
//                } else {
//                    pauseMeasurements(gatt, NoninHandler.SAMPLE_RATE_SEC.toLong())
//                }
            } catch (exception: Exception) {
                DefaultCallbacksHelper.onErrorDefault(
                    NoninHandler.TAG,
                    "Couldn't handle measurements after parsing",
                    exception,
                    logger
                )
            }
        }
    }

    private fun onRespirationNotification(gatt: BluetoothGatt?, value: ByteArray?) {
        value?.also { bytes ->
            val respiratoryRate = bytes[4].toInt() and 0xff
            gatt?.device?.address?.also {
                synchronized(lastRespiratoryRates) {
                    lastRespiratoryRates[it] = respiratoryRate
                }
            }
            logger.i(
                NoninHandler.TAG,
                "device ${gatt?.device?.address} - nonin respiratory rate message, respiratory: ${if (isRespiratoryRateValueMissing(
                        respiratoryRate
                    )
                ) "missing" else respiratoryRate}"
            )
        }
    }

    private fun isHeartRateValueMissing(value: Int) = isMeasurmentMissing(
        value,
        NoninHandler.NoninGattAttributes.OximeteryService.Characteritics.HEART_RATE_MISSING_VALUE
    )

    private fun isSaturationValueMissing(value: Int) = isMeasurmentMissing(
        value,
        NoninHandler.NoninGattAttributes.OximeteryService.Characteritics.SATURATION_MISSING_VALUE
    )

    private fun isRespiratoryRateValueMissing(value: Int) = isMeasurmentMissing(
        value,
        NoninHandler.NoninGattAttributes.OximeteryService.Characteritics.RESPIRATORY_RATE_MISSING_VALUE
    )

    private fun isMeasurmentMissing(value: Int, missingValue: Int) = value == missingValue

    override fun close() {
//        sensors.removeObserver(sensorsObserver)
//        disconnectAll()
    }

    fun start() {
        initializeSensors()
    }

    private fun initializeSensors() {
        sensors = patientRepository.getSensors().apply {
            observeForever {
                connectedDevices.filter { gatt -> !isDeviceInSensors(gatt.device.address) }
                    .forEach { gatt ->
                        logger.i(
                            TAG,
                            "found connected device ${gatt.device.address} not in sensors, about to disconnect it"
                        )
                        disconnect(gatt)
                    }
                if (!alreadyScheduledScans) {
                    maintainScans()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun maintainSensors() {
        synchronized(sharedResourcesLock) {
            Log.d(TAG, "in maintain sensors, printing before lists for details")
            printScanQueue()
            printConnectingDevices()
            printConnectedDevices()
            scanQueue.removeIf { address ->
                sensors.value?.find { it.address == address } == null
                        || connectingDevices.find { it == address } != null
                        || connectedDevices.find { it.device.address == address } != null
            } // remove from scan queue unnecessery sensors
            Log.d(TAG, "scan queue after remove unnecesseries")
            printScanQueue()
            sensors.value?.filter { !it.isConnected && !scanQueue.contains(it.address) }?.forEach {
                putInScanQueue(it.address)
            }
            Log.d(TAG, "scan queue finished")
            printScanQueue()
        }
    }

    private fun printScanQueue() {
        var str = ""
        scanQueue.forEach { str += "$it, " }
        Log.d(TAG, "scan queue: [$str]")
    }

    private fun printConnectingDevices() {
        var str = ""
        connectingDevices.forEach { str += it }
        Log.d(TAG, "connecting list: $str")
    }

    private fun printConnectedDevices() {
        var str = ""
        connectedDevices.forEach { str += it.device.address }
        Log.d(TAG, "connected devices: $str")
    }

//    private fun scanFirstInQueue() {
//        val address: String?
//        synchronized(sharedResourcesLock) {
//            address = scanQueue.firstOrNull()
//        }
//        address?.apply { scan(this) }
//    }

    private fun maintainScans() {
        alreadyScheduledScans = true
        maintainScansTimer?.cancel()
        sensors.value?.also { sensors ->
            val addressesToScan =
                sensors.filter { BluetoothAdapter.checkBluetoothAddress(it.address) && !it.isConnected }
                    .map { it.address }
            if (addressesToScan.isNotEmpty()) {
                scan(addressesToScan)
            }
        }
        maintainScansTimer = Timer("maintain scans", false).apply {
            schedule(WAIT_BETWEEN_SCANS_PERIOD.toLong()) {
                maintainScans()
            }
        }
//        maintainSensors()
//        scanFirstInQueue()
    }

    private val sensorsObserver = Observer<List<Sensor>> {
        maintainScans()

//    private val sensorsObserver = Observer<List<Sensor>> {
//        scanDisposables.forEach { address, scanDisposable ->
//            scanDisposable.dispose()
//        }
//
//        scanDisposables.clear()
//
//        val connectedAddresses = connectedDevicesList.map {
//            it.getDevice()?.macAddress
//        }
//
//        val sensorsAddresses = it.map {
//            it.address
//        }
//
//        // for each sensor that in db and not connected - connect
//        it.forEach {
//            if (!it.isConnected) {
//                scan(it.address)
//            }
//        }
////        sensorsAddresses.forEach {
////            if (!connectedAddresses.contains(it)) {
////                scan(it)
////            }
////        }
//
//        val unrelevantAddresses = connectedAddresses.filter {
//            !sensorsAddresses.contains(it)
//        }
//
//        unrelevantAddresses.forEach {
//            if (it != null) {
//                disconnect(it)
//            }
//        }
//    }
    }

//    private val connectedDevicesList = mutableListOf<BleDeviceHandler>()

    private fun startScan(
        scanFilters: List<android.bluetooth.le.ScanFilter>,
        scanSettings: android.bluetooth.le.ScanSettings,
        scanCallback: ScanCallback
    ) {
        setIsScanning(true)
        currentScanCallback = scanCallback
        bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)
    }

    private fun stopScan() {
        if (getIsScanning()) {
            currentScanCallback?.also {
                bluetoothLeScanner.stopScan(currentScanCallback)
            } ?: logger.e(TAG, "stop scan with null currentScanCallback")
            setIsScanning(false)
//            continueScanQueue()
//            maintainScans()
        }
    }

    fun putInScanQueue(address: String) {
        synchronized(sharedResourcesLock) {
            if (!scanQueue.contains(address)) {
                logger.d(TAG, "added $address to scan queue")
                scanQueue.add(address)
            }
        }
    }

    private fun findDeviceInConnectedList(address: String): BluetoothGatt? {
        var gatt: BluetoothGatt?
        synchronized(sharedResourcesLock) {
            gatt = connectedDevices.find { it.device.address == address }
        }
        return gatt
    }

    private fun isDeviceInConnectedList(address: String) =
        findDeviceInConnectedList(address) != null

    private fun findDeviceInConnectingList(address: String): String? {
        var res: String?
        synchronized(sharedResourcesLock) {
            res = connectingDevices.find { it == address }
        }
        return res
    }

    private fun isDeviceInConnectingList(address: String) =
        findDeviceInConnectingList(address) != null

    private fun getStringListOfAddresses(addresses: List<String>): String {
        var str = "["
        addresses.forEach { str += "$it, " }
        return str + "]"
    }


    fun scan(addresses: List<String>) {
//        executor.execute {
        logger.d(TAG, "in scan with address")
        if (addresses.any { !BluetoothAdapter.checkBluetoothAddress(it) }) {
            logger.i(TAG, "called scan with illegal address")
        } else {
//                if (isDeviceInConnectedList(address)) {
//                    Log.d(TAG, "try to scan connected device ${address}, not proceeding")
//                    return@execute
//                }
//
//                if (isDeviceInConnectingList(address)) {
//                    Log.d(TAG, "try to scan connecting device ${address}, not proceeding")
//                    return@execute
//                }

            val filters = addresses.map {
                android.bluetooth.le.ScanFilter.Builder()
                    .setDeviceAddress(it)
                    .build()
            }

//                val filters = android.bluetooth.le.ScanFilter.Builder()
//                    .setDeviceAddress(address)
//                    .build()

            if (!getIsScanning()) {
//                    Log.d(TAG, "about to scan for address $address")
                currentScanThread?.interrupt()
                currentScanThread = object : Thread() {
                    override fun run() {
                        super.run()
                        logger.d(TAG, "about to scan ${getStringListOfAddresses(addresses)}")
                        handler.postDelayed(
                            {
                                stopScan()
                            }, SCAN_PERIOD.toLong()
                        )
                        startScan(filters, scanSettings, generateScanCallback())
                    }
                }.apply { start() }
            } else {
                logger.d(
                    TAG,
                    "can't scan ${getStringListOfAddresses(addresses)} now because is scanning, wait to turn"
                )
            }
        }
//        }

//        val settings = buildScanSettings()
//        val filters = buildScanFilter(address)
//
//        Log.i(TAG, "Start scan for address $address")
//        rxBleClient.scanBleDevices(settings, filters)
//            .subscribe(
//                {
//                    onScanResult(it)
//                }, {
//                    DefaultCallbacksHelper.onErrorDefault(TAG, "Scan for address $address failed", it)
//                }, {
//                    DefaultCallbacksHelper.onSuccessDefault(TAG, "Scan for address $address completed")
//                }, {
//                    DefaultCallbacksHelper.onSuccessDefault(TAG, "Scan for address $address was subscribed")
//                    scanDisposables.put(address, it)
//                }
//            )
    }

    private fun connect(device: BluetoothDevice) {
//        executor.execute {
        if (!isDeviceConnected(device.address)) {
            if (!isDeviceConnecting(device.address)) {
                logger.d(TAG, "about to connect to ${device.address}")
//            synchronized(sharedResourcesLock) {
                connectingDevices.add(device.address)
//                Log.d(TAG, "added ${device.address} to connecting devices list")
//                printConnectingDevices()
//            }
                val gattCallback = GattCallback().apply { delegate = this@BluetoothController }
                device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                logger.d(
                    TAG,
                    "try to connect to connected device ${device.address}, doesnt continue"
                )
            }

        } else {
            Log.d(TAG, "try to connect to connected device ${device.address}, doesnt continue")
        }
//        }
    }

    private fun disconnect(gatt: BluetoothGatt) {
        Log.d(TAG, "disconnect from device ${gatt.device.address}")
        gatt.disconnect()
    }


//    private fun disconnectAll() {
//        Log.i(TAG, "disconnecting all devices")
//        connectedDevicesList.forEach {
//            disconnect(it)
//        }
//    }
//
//    private fun disconnect(address: String) {
//        val deviceHandler = findDeviceInConnectedDevices(address)
//        deviceHandler?.apply {
//            disconnect(this)
//        }
//    }
//
//    private fun onScanResult(scanResult: ScanResult, scanDisposable: Disposable? = null) {
//        DefaultCallbacksHelper.onSuccessDefault(
//            TAG,
//            "Scan for address ${scanResult.bleDevice.macAddress} succeeded, try to connect"
//        )
//        scanDisposable?.dispose()
//        scanDisposables[scanResult.bleDevice.macAddress]?.dispose()
//        getBleDeviceHandlerByScanResult(scanResult)?.apply {
//            connect(this)
//        } ?: Log.i(TAG, "Device from scan isn't from the supported sensors")
//    }
//
//    private fun disconnect(deviceHandler: BleDeviceHandler) {
//        val address = deviceHandler.getDevice().macAddress
//        Log.i(TAG, "disconnecting from address $address")
//        connectedDevicesList.remove(deviceHandler)
//        deviceHandler.getObserveConnectionStateDisposable()?.dispose()
//        deviceHandler.getConnectionDisposable()?.dispose()
//    }
//
//    private fun connect(bleDeviceHandler: BleDeviceHandler, autoConnect: Boolean = false) {
//        val device = bleDeviceHandler.getDevice()
//        device.establishConnection(autoConnect)
//            .subscribe(
//                {
//                    onConnectionEstablished(bleDeviceHandler, it)
//                }, {
//                    DefaultCallbacksHelper.onErrorDefault(
//                        TAG,
//                        "Establish connection to device with address ${device.macAddress} failed",
//                        it
//                    )
//                }, {
//
//                }, {
//                    bleDeviceHandler.setConnectionDisposable(it)
//                }
//            )
//    }
//
////    private fun onConnectionEstablished(
////        bleDeviceHandler: BleDeviceHandler,
////        connection: RxBleConnection
////    ) {
////        DefaultCallbacksHelper.onSuccessDefault(
////            TAG,
////            "Establish connection to device with address ${bleDeviceHandler.getDevice().macAddress} succeeded"
////        )
////        patientRepository.setSensorIsConnected(bleDeviceHandler.getDevice().macAddress, true)
////        bleDeviceHandler.onConnected(connection)
////        observeConnectionState(bleDeviceHandler)
////    }
//
//    private fun observeConnectionState(bleDeviceHandler: BleDeviceHandler) {
//        bleDeviceHandler.getDevice().observeConnectionStateChanges()
//            .subscribe(
//                {
//                    onConnectionStateChanged(bleDeviceHandler, it)
//                }, {
//                    DefaultCallbacksHelper.onErrorDefault(
//                        TAG,
//                        "observe to connection state for address ${bleDeviceHandler.getDevice().macAddress} failed",
//                        it
//                    )
//                }, {
//
//                }, {
//                    bleDeviceHandler.setConnectionDisposable(it)
//                }
//            )
//    }
//
//    private fun onConnectionStateChanged(
//        bleDeviceHandler: BleDeviceHandler,
//        connectionState: RxBleConnection.RxBleConnectionState
//    ) {
//        when (connectionState) {
//            RxBleConnection.RxBleConnectionState.DISCONNECTED -> onDisconnected(bleDeviceHandler)
//            RxBleConnection.RxBleConnectionState.CONNECTED -> onConnected(bleDeviceHandler)
//            RxBleConnection.RxBleConnectionState.DISCONNECTING -> onDisconnecting(bleDeviceHandler)
//            RxBleConnection.RxBleConnectionState.CONNECTING -> onConnecting(bleDeviceHandler)
//        }
//    }
//
//    private fun onConnected(bleDeviceHandler: BleDeviceHandler) {
//        DefaultCallbacksHelper.onSuccessDefault(
//            TAG,
//            "Device with address ${bleDeviceHandler.getDevice().macAddress} connected"
//        )
//    }
//
//    private fun onDisconnected(bleDeviceHandler: BleDeviceHandler) {
//        DefaultCallbacksHelper.onSuccessDefault(
//            TAG,
//            "Device with address ${bleDeviceHandler.getDevice().macAddress} disconnected"
//        )
//
//        val deviceHandler = findDeviceInConnectedDevices(bleDeviceHandler.getDevice().macAddress)
//
//        connectedDevicesList.remove(deviceHandler)
//        patientRepository.setSensorIsConnected(bleDeviceHandler.getDevice().macAddress, false)
//    }
//
//    private fun onConnecting(bleDeviceHandler: BleDeviceHandler) {
//        DefaultCallbacksHelper.onSuccessDefault(
//            TAG,
//            "Device with address ${bleDeviceHandler.getDevice().macAddress} connecting"
//        )
//    }
//
//    private fun onDisconnecting(bleDeviceHandler: BleDeviceHandler) {
//        DefaultCallbacksHelper.onSuccessDefault(
//            TAG,
//            "Device with address ${bleDeviceHandler.getDevice().macAddress} disconnecting"
//        )
//    }
//
//    private fun onUnintentionalDisconnect(bleDeviceHandler: BleDeviceHandler) {
//        scan(bleDeviceHandler.getDevice().macAddress)
//        disconnect(bleDeviceHandler)
//    }
//
//    private fun findDeviceInConnectedDevices(address: String) = connectedDevicesList.filter {
//        it.getDevice().macAddress == address
//    }.firstOrNull()
//
//    private fun getBleDeviceHandlerByScanResult(scanResult: ScanResult): BleDeviceHandler? {
//        val handler: BleDeviceHandler? = scanResult.bleDevice.name?.let {
//            if (it.contains("Nonin")) {
//                return NoninHandler(context, scanResult.bleDevice).also {
//                    connectedDevicesList.add(it)
//                }
//            }
//            return null
//        }
//
//        return handler
//    }
//
//    private fun buildScanSettings() = ScanSettings.Builder()
//        .build()
//
//    private fun buildScanFilter(address: String) = ScanFilter.Builder()
//        .setDeviceAddress(address)
//        .build()

    companion object {
        const val TAG = "BluetoothController"
        private const val SCAN_PERIOD = 10000
        private const val WAIT_BETWEEN_SCANS_PERIOD = 15000
        val CLIENT_CONFIG_DESCRIPTOR_UUID: UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}