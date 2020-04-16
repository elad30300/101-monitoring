package com.example.a101_monitoring.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a101_monitoring.data.model.PatientIdentityFieldType
import com.example.a101_monitoring.di.scope.PatientManualMeasurmentsScope
import javax.inject.Inject

@PatientManualMeasurmentsScope
class PatientManualMeasurmentsViewModel @Inject constructor() : ViewModel() {
    var patientId: PatientIdentityFieldType? = null
}
