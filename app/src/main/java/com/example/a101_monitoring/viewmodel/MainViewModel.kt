package com.example.a101_monitoring.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a101_monitoring.di.scope.MainActivityScope
import com.example.a101_monitoring.repository.MeasurementsRepository
import com.example.a101_monitoring.repository.PatientRepository
import com.example.a101_monitoring.repository.ReleaseReasonsRepository
import com.example.a101_monitoring.repository.VersioningRepository
import javax.inject.Inject

@MainActivityScope
class MainViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val versioningRepository: VersioningRepository
) : ViewModel() {

    fun getCheckPatientExistState() = patientRepository.getCheckPatientExistState()

    fun getRegisterPatientState() = patientRepository.getRegisterPatientState()

    fun getSignInPatientState() = patientRepository.getSignInPatientState()

    fun getSubmitSensorToPatientState() = patientRepository.getSubmitSensorToPatientState()

    fun getBloodPressureState() = patientRepository.getBloodPressureState()

    fun getBodyTemperatureState() = patientRepository.getBodyTemperatureState()

    fun getReleasePatientState() = patientRepository.getReleasePatientState()

    fun getLatestVersion(phoneId: String, phoneNumber: String, version: String) =
        versioningRepository.getLatestVersion(phoneId, phoneNumber, version)
}