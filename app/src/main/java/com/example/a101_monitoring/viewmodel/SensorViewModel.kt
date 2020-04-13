package com.example.a101_monitoring.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a101_monitoring.di.scope.MainActivityScope
import com.example.a101_monitoring.repository.PatientRepository
import javax.inject.Inject

@MainActivityScope
class SensorViewModel @Inject constructor(
    private val patientRepository: PatientRepository
) : ViewModel() {

    val sensors = patientRepository.getSensors()

}