package com.example.a101_monitoring.di.component

import com.example.a101_monitoring.di.scope.PatientManualMeasurmentsScope
import com.example.a101_monitoring.ui.PatientManualMeasurmentsFragment
import dagger.Subcomponent

@PatientManualMeasurmentsScope
@Subcomponent
interface PatientManualMeasurmentsComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): PatientManualMeasurmentsComponent
    }

    fun inject(patientManualMeasurmentsFragment: PatientManualMeasurmentsFragment)
}