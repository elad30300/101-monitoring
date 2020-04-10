package com.example.a101_monitoring.repository

import com.example.a101_monitoring.data.model.Patient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepository @Inject constructor() {

    fun getPatients(): List<Patient> {
        return listOf(
            Patient("123", 313599466, 1, "1", "1", "2344", "2568", false, 1, false),
            Patient("123", 123456789, 1, "1", "1", "2344", "2568", false, 1, false)
        )
    }

}