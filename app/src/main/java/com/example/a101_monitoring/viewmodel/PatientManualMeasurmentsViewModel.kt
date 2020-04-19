package com.example.a101_monitoring.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a101_monitoring.data.model.PatientIdentityFieldType
import com.example.a101_monitoring.di.scope.PatientManualMeasurmentsScope
import com.example.a101_monitoring.repository.PatientRepository
import com.example.a101_monitoring.utils.TimeHelper
import javax.inject.Inject

@PatientManualMeasurmentsScope
class PatientManualMeasurmentsViewModel @Inject constructor(
    private val patientRepository: PatientRepository
) : ViewModel() {

    fun sendBloodPressure(diastolic: Int, systolic: Int, patientId: PatientIdentityFieldType) {
        patientRepository.sendBloodPressure(diastolic, systolic, patientId)
    }

    fun sendBodyTemperature(temperature: Float, patientId: PatientIdentityFieldType) {
        patientRepository.sendBodyTemperature(temperature, patientId)
    }

    fun getBloodPressureState() = patientRepository.getBloodPressureState()

    fun getBodyTemperatureState() = patientRepository.getBodyTemperatureState()

}
