package com.example.a101_monitoring.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.di.scope.PatientsListFragmentScope
import com.example.a101_monitoring.repository.PatientRepository
import javax.inject.Inject

@PatientsListFragmentScope
class PatientsListViewModel @Inject constructor(val patientRepository: PatientRepository): ViewModel() {

    fun getPatients(): List<Patient> = patientRepository.getPatients()

}