package com.example.a101_monitoring.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a101_monitoring.repository.PatientRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatesViewModel @Inject constructor(
    private val patientRepository: PatientRepository
) : ViewModel() {

    val registerPatientState = patientRepository.getRegisterPatientState()

}