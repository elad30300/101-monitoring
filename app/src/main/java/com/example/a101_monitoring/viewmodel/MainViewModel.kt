package com.example.a101_monitoring.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a101_monitoring.di.scope.MainActivityScope
import com.example.a101_monitoring.repository.MeasurementsRepository
import com.example.a101_monitoring.repository.PatientRepository
import com.example.a101_monitoring.repository.ReleaseReasonsRepository
import javax.inject.Inject

@MainActivityScope
class MainViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val measurementsRepository: MeasurementsRepository
) : ViewModel() {

    fun getRegisterPatientState() = patientRepository.getRegisterPatientState()

    fun getSignInPatientState() = patientRepository.getSignInPatientState()

    fun getSubmitSensorToPatientState() = patientRepository.getSubmitSensorToPatientState()

}