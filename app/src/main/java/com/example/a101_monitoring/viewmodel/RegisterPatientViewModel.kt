package com.example.a101_monitoring.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.repository.PatientRepository
import javax.inject.Inject

class RegisterPatientViewModel @Inject constructor(
    private val patientRepository: PatientRepository
) : ViewModel() {

    fun registerPatient(
        id: Int,
        deptId: Int,
        room: String,
        bed: String,
        haitiId: String,
        registeredDoctor: String,
        isCitizen: Boolean,
        isOxygen: Int,
        isActive: Boolean
    ) {
        patientRepository.registerPatient(
            Patient("a", id, deptId, room, bed, haitiId, registeredDoctor, isCitizen, isOxygen, isActive)
        )
    }

}
