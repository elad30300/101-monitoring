package com.example.a101_monitoring.di.component

import com.example.a101_monitoring.di.module.ApplicationSubcomponentModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    ApplicationSubcomponentModule::class
])
interface ApplicationComponent {
    fun patientsListComponent(): PatientsListFragmentComponent.Factory
}