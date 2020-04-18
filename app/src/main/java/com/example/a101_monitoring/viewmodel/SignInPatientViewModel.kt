package com.example.a101_monitoring.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a101_monitoring.data.model.PatientIdentityFieldType
import com.example.a101_monitoring.repository.PatientRepository
import javax.inject.Inject

class SignInPatientViewModel @Inject constructor(
    private val patientRepository: PatientRepository
) : ViewModel() {

    fun signIn(patientId: PatientIdentityFieldType) {
        patientRepository.signIn(patientId)
    }

    fun getSignInPatientState() = patientRepository.getSignInPatientState()

}
