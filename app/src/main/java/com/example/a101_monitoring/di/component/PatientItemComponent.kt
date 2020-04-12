package com.example.a101_monitoring.di.component

import com.example.a101_monitoring.viewmodel.PatientItemViewModel
import dagger.Subcomponent

@Subcomponent
interface PatientItemComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): PatientItemComponent
    }

    fun inject(patientItemViewModel: PatientItemViewModel)
}