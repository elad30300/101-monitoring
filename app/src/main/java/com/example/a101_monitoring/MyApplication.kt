package com.example.a101_monitoring

import android.app.Application
import com.example.a101_monitoring.di.component.DaggerApplicationComponent

class MyApplication : Application() {
    val applicationComponent = DaggerApplicationComponent.builder()
        .build()
}