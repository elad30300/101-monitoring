package com.example.a101_monitoring.repository

import com.example.a101_monitoring.data.dao.PatientDao
import com.example.a101_monitoring.data.model.Patient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepository @Inject constructor(private val patientDao: PatientDao) {

    fun getPatients() = patientDao.getAll()

    fun registerPatient(patient: Patient) {
        patientDao.insertPatients(patient)
    }

}