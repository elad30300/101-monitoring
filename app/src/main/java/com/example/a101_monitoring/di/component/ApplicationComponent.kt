package com.example.a101_monitoring.di.component

import com.example.a101_monitoring.MainActivity
import com.example.a101_monitoring.MyApplication
import com.example.a101_monitoring.di.module.ApplicationModule
import com.example.a101_monitoring.di.module.ApplicationSubcomponentModule
import com.example.a101_monitoring.di.module.BluetoothModule
import com.example.a101_monitoring.receiver.SensorAddressUpdatedBroadcastReceiver
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    ApplicationModule::class,
    ApplicationSubcomponentModule::class,
    BluetoothModule::class
])
interface ApplicationComponent {

    fun patientsListComponent(): PatientsListFragmentComponent.Factory

    fun registerPatientComponent(): RegisterPatientComponent.Factory

    fun patientManualMeasurmentsComponent(): PatientManualMeasurmentsComponent.Factory

    fun patientItemComponent(): PatientItemComponent.Factory

    fun sensorChooseComponent(): SensorChooseComponent.Factory

    fun mainActivityComponent(): MainActivityComponent.Factory


    fun inject(sensorAddressUpdatedBroadcastReceiver: SensorAddressUpdatedBroadcastReceiver)

//    fun inject(mainActivity: MainActivity)
}