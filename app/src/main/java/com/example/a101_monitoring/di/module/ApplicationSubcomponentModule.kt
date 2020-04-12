package com.example.a101_monitoring.di.module

import com.example.a101_monitoring.di.component.PatientManualMeasurmentsComponent
import com.example.a101_monitoring.di.component.PatientsListFragmentComponent
import com.example.a101_monitoring.di.component.RegisterPatientComponent
import dagger.Module

@Module(subcomponents = [
    PatientsListFragmentComponent::class,
    PatientManualMeasurmentsComponent::class,
    RegisterPatientComponent::class
])
class ApplicationSubcomponentModule {

}