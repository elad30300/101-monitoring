package com.example.a101_monitoring.di.module

import com.example.a101_monitoring.di.component.*
import dagger.Module

@Module(subcomponents = [
    PatientsListFragmentComponent::class,
    PatientManualMeasurmentsComponent::class,
    RegisterPatientComponent::class,
    PatientItemComponent::class,
    SensorChooseComponent::class,
    MainActivityComponent::class,
    ReleasePatientComponent::class,
    SignInComponent::class
])
class ApplicationSubcomponentModule {

}