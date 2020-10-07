package com.example.a101_monitoring.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a101_monitoring.data.model.PatientIdentityFieldType
import com.example.a101_monitoring.data.model.ReleaseReason
import com.example.a101_monitoring.di.scope.ReleasePatientScope
import com.example.a101_monitoring.repository.PatientRepository
import com.example.a101_monitoring.repository.ReleaseReasonsRepository
import javax.inject.Inject

@ReleasePatientScope
class ReleasePatientDialogViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val releaseReasonsRepository: ReleaseReasonsRepository
) : ViewModel() {

    val releaseReasons = releaseReasonsRepository.getReleaseReasons()

    fun releasePatient(patientId: PatientIdentityFieldType, releaseReason: ReleaseReason) {
        patientRepository.releasePatient(patientId, releaseReason)
    }

    fun removePatientLocally(patientId: PatientIdentityFieldType) {
        patientRepository.removePatientLocally(patientId)
    }

    fun checkReleaseAccessPassword(password: String) = releaseReasonsRepository.checkReleaseAccessPassword(password)

}
