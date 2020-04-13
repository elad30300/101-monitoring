package com.example.a101_monitoring.bluetooth.handlers

import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.disposables.Disposable

abstract class BleDeviceHandler(
    private val device: RxBleDevice
) {

//    private var device: RxBleDevice? = null
    private var connection: RxBleConnection? = null
    private var connectionDisposable: Disposable? = null
    private var observeConnectionStateDisposable: Disposable? = null

    abstract fun onConnected()

    open fun onConnected(connection: RxBleConnection) {
        setConnection(connection)
    }

    fun getDevice() = device

    fun getConnection() = connection

    fun getConnectionDisposable() = connectionDisposable

    fun getObserveConnectionStateDisposable() = observeConnectionStateDisposable

//    fun setDevice(device: RxBleDevice) {
//        this.device = device
//    }

    fun setConnection(connection: RxBleConnection?) {
        this.connection = connection
    }

    fun setConnectionDisposable(disposable: Disposable?) {
        this.connectionDisposable = disposable
    }

    fun setObserveConnectionStateDisposable(disposable: Disposable?) {
        this.observeConnectionStateDisposable = disposable
    }
}