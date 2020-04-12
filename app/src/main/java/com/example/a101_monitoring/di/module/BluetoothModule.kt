package com.example.a101_monitoring.di.module

import android.content.Context
import com.polidea.rxandroidble2.RxBleClient
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BluetoothModule(private val context: Context) {

    @Singleton
    @Provides
    fun provideRxBleClient(context: Context) = RxBleClient.create(context)

}