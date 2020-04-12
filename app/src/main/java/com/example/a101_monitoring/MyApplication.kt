package com.example.a101_monitoring

import android.app.Application
import com.example.a101_monitoring.di.component.DaggerApplicationComponent
import com.example.a101_monitoring.di.module.ApplicationModule
import com.example.a101_monitoring.di.module.BluetoothModule

class MyApplication : Application() {
    val applicationComponent = DaggerApplicationComponent.builder()
        .applicationModule(ApplicationModule(this))
        .bluetoothModule(BluetoothModule(this))
        .build()
}