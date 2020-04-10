package com.example.a101_monitoring.di.component

import com.example.a101_monitoring.di.module.ApplicationModule
import com.example.a101_monitoring.di.module.ApplicationSubcomponentModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    ApplicationModule::class,
    ApplicationSubcomponentModule::class
])
interface ApplicationComponent {

    fun patientsListComponent(): PatientsListFragmentComponent.Factory

    fun registerPatientComponent(): RegisterPatientComponent.Factory

}