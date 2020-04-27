package com.example.a101_monitoring.bluetooth

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.a101_monitoring.bluetooth.handlers.BleDeviceHandler
import com.example.a101_monitoring.bluetooth.handlers.NoninHandler
import com.example.a101_monitoring.data.model.Sensor
import com.example.a101_monitoring.repository.PatientRepository
import com.example.a101_monitoring.utils.DefaultCallbacksHelper
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.disposables.Disposable
import java.io.Closeable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothController @Inject constructor(
    private val context: Application,
    private val rxBleClient: RxBleClient,
    private val patientRepository: PatientRepository
) : Closeable {

    lateinit var sensors: LiveData<List<Sensor>>
    private val scanDisposables = mutableMapOf<String, Disposable>()

    override fun close() {
        sensors.removeObserver(sensorsObserver)
        disconnectAll()
    }

    fun start() {
        initializeSensors()
    }

    private fun initializeSensors() {
        sensors = patientRepository.getSensors().apply {
            observeForever(sensorsObserver)
        }
    }

    private val sensorsObserver = Observer<List<Sensor>> {
        scanDisposables.forEach { address, scanDisposable ->
            scanDisposable.dispose()
        }

        scanDisposables.clear()

        val connectedAddresses = connectedDevicesList.map {
            it.getDevice()?.macAddress
        }

        val sensorsAddresses = it.map {
            it.address
        }

        // for each sensor that in db and not connected - connect
        it.forEach {
            if (!it.isConnected) {
                scan(it.address)
            }
        }
//        sensorsAddresses.forEach {
//            if (!connectedAddresses.contains(it)) {
//                scan(it)
//            }
//        }

        val unrelevantAddresses = connectedAddresses.filter {
            !sensorsAddresses.contains(it)
        }

        unrelevantAddresses.forEach {
            if (it != null) {
                disconnect(it)
            }
        }
    }

    private val connectedDevicesList = mutableListOf<BleDeviceHandler>()

    fun scan(address: String) {

        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            Log.i(TAG, "called scan with illegal address")
            return
        }

        val settings = buildScanSettings()
        val filters = buildScanFilter(address)

        Log.i(TAG, "Start scan for address $address")
        rxBleClient.scanBleDevices(settings, filters)
            .subscribe(
                {
                    onScanResult(it)
                }, {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "Scan for address $address failed", it)
                }, {
                    DefaultCallbacksHelper.onSuccessDefault(TAG, "Scan for address $address completed")
                }, {
                    DefaultCallbacksHelper.onSuccessDefault(TAG, "Scan for address $address was subscribed")
                    scanDisposables.put(address, it)
                }
            )
    }

    private fun disconnectAll() {
        Log.i(TAG, "disconnecting all devices")
        connectedDevicesList.forEach {
            disconnect(it)
        }
    }

    private fun disconnect(address: String) {
        val deviceHandler = findDeviceInConnectedDevices(address)
        deviceHandler?.apply {
            disconnect(this)
        }
    }

    private fun onScanResult(scanResult: ScanResult, scanDisposable: Disposable? = null) {
        DefaultCallbacksHelper.onSuccessDefault(TAG, "Scan for address ${scanResult.bleDevice.macAddress} succeeded, try to connect")
        scanDisposable?.dispose()
        scanDisposables[scanResult.bleDevice.macAddress]?.dispose()
        getBleDeviceHandlerByScanResult(scanResult)?.apply {
            connect(this)
        } ?: Log.i(TAG, "Device from scan isn't from the supported sensors")
    }

    private fun disconnect(deviceHandler: BleDeviceHandler) {
        val address = deviceHandler.getDevice().macAddress
        Log.i(TAG, "disconnecting from address $address")
        connectedDevicesList.remove(deviceHandler)
        deviceHandler.getObserveConnectionStateDisposable()?.dispose()
        deviceHandler.getConnectionDisposable()?.dispose()
    }

    private fun connect(bleDeviceHandler: BleDeviceHandler, autoConnect: Boolean = false) {
        val device = bleDeviceHandler.getDevice()
        device.establishConnection(autoConnect)
            .subscribe(
                {
                    onConnectionEstablished(bleDeviceHandler, it)
                }, {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "Establish connection to device with address ${device.macAddress} failed", it)
                }, {

                }, {
                    bleDeviceHandler.setConnectionDisposable(it)
                }
            )
    }

    private fun onConnectionEstablished(bleDeviceHandler: BleDeviceHandler, connection: RxBleConnection) {
        DefaultCallbacksHelper.onSuccessDefault(TAG, "Establish connection to device with address ${bleDeviceHandler.getDevice().macAddress} succeeded")
        patientRepository.setSensorIsConnected(bleDeviceHandler.getDevice().macAddress, true)
        bleDeviceHandler.onConnected(connection)
        observeConnectionState(bleDeviceHandler)
    }

    private fun observeConnectionState(bleDeviceHandler: BleDeviceHandler) {
        bleDeviceHandler.getDevice().observeConnectionStateChanges()
            .subscribe(
                {
                    onConnectionStateChanged(bleDeviceHandler, it)
                }, {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "observe to connection state for address ${bleDeviceHandler.getDevice().macAddress} failed", it)
                }, {

                }, {
                    bleDeviceHandler.setConnectionDisposable(it)
                }
            )
    }

    private fun onConnectionStateChanged(bleDeviceHandler: BleDeviceHandler, connectionState: RxBleConnection.RxBleConnectionState) {
        when (connectionState) {
            RxBleConnection.RxBleConnectionState.DISCONNECTED -> onDisconnected(bleDeviceHandler)
            RxBleConnection.RxBleConnectionState.CONNECTED -> onConnected(bleDeviceHandler)
            RxBleConnection.RxBleConnectionState.DISCONNECTING -> onDisconnecting(bleDeviceHandler)
            RxBleConnection.RxBleConnectionState.CONNECTING -> onConnecting(bleDeviceHandler)
        }
    }

    private fun onConnected(bleDeviceHandler: BleDeviceHandler) {
        DefaultCallbacksHelper.onSuccessDefault(TAG, "Device with address ${bleDeviceHandler.getDevice().macAddress} connected")
    }

    private fun onDisconnected(bleDeviceHandler: BleDeviceHandler) {
        DefaultCallbacksHelper.onSuccessDefault(TAG, "Device with address ${bleDeviceHandler.getDevice().macAddress} disconnected")

        val deviceHandler = findDeviceInConnectedDevices(bleDeviceHandler.getDevice().macAddress)

        connectedDevicesList.remove(deviceHandler)
        patientRepository.setSensorIsConnected(bleDeviceHandler.getDevice().macAddress, false)
    }

    private fun onConnecting(bleDeviceHandler: BleDeviceHandler) {
        DefaultCallbacksHelper.onSuccessDefault(TAG, "Device with address ${bleDeviceHandler.getDevice().macAddress} connecting")
    }

    private fun onDisconnecting(bleDeviceHandler: BleDeviceHandler) {
        DefaultCallbacksHelper.onSuccessDefault(TAG, "Device with address ${bleDeviceHandler.getDevice().macAddress} disconnecting")
    }

    private fun onUnintentionalDisconnect(bleDeviceHandler: BleDeviceHandler) {
        scan(bleDeviceHandler.getDevice().macAddress)
        disconnect(bleDeviceHandler)
    }

    private fun findDeviceInConnectedDevices(address: String) = connectedDevicesList.filter {
                                                            it.getDevice().macAddress == address
                                                        }.firstOrNull()

    private fun getBleDeviceHandlerByScanResult(scanResult: ScanResult): BleDeviceHandler? {
        val handler: BleDeviceHandler? = scanResult.bleDevice.name?.let {
            if (it.contains("Nonin")) {
                return NoninHandler(context, scanResult.bleDevice).also {
                    connectedDevicesList.add(it)
                }
            }
            return null
        }

        return handler
    }

    private fun buildScanSettings() = ScanSettings.Builder()
                                        .build()

    private fun buildScanFilter(address: String) = ScanFilter.Builder()
                                                    .setDeviceAddress(address)
                                                    .build()

    companion object {
        const val TAG = "BluetoothController"
    }
}