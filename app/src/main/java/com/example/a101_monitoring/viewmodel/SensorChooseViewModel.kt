package com.example.a101_monitoring.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a101_monitoring.repository.PatientRepository
import javax.inject.Inject

class SensorChooseViewModel @Inject constructor(
    private val patientRepository: PatientRepository
) : ViewModel() {

    fun getSensor(patientId: Int) = patientRepository.getSensorAddress(patientId)

    fun setSensor(patientId: Int, sensorAddress: String) {
        patientRepository.setSensor(patientId, sensorAddress)
    }

}
