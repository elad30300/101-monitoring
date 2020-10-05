package com.example.a101_monitoring.di.module

import android.app.Application
import android.content.Context
import com.polidea.rxandroidble2.RxBleClient
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Named
import javax.inject.Singleton

@Module
class BluetoothModule(private val context: Application) {

    @Singleton
    @Provides
    fun provideRxBleClient() = RxBleClient.create(context)

    @Singleton
    @Provides
    @Named("BleExecutor")
    fun provideBleExecutor(): Executor = Executors.newSingleThreadExecutor()

}