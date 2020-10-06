package com.example.a101_monitoring.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.di.scope.PatientsListFragmentScope
import com.example.a101_monitoring.repository.PatientRepository
import javax.inject.Inject

@PatientsListFragmentScope
class PatientsListViewModel @Inject constructor(val patientRepository: PatientRepository): ViewModel() {

    val patients = MutableLiveData<List<Patient>>(listOf())

    fun getAllPatients() = patientRepository.getPatients()

    init {
        patientRepository.getPatients().observeForever {newPatients ->
            patients.value?.also {currentPatients ->
                if (arePatientsListDifferent(currentPatients, newPatients)) {
                    patients.postValue(newPatients)
                }
            } ?: patients.postValue(listOf())
        }
    }

    fun arePatientsListDifferent(p1: List<Patient>, p2: List<Patient>): Boolean {
        return p1.any { p -> p2.find { p.identityId == it.identityId } == null }
                || p2.any { p -> p1.find { p.identityId == it.identityId } == null }
    }

//    val patients: LiveData<List<Patient>> = getAllPatients()
//
//    fun getAllPatients() = patientRepository.getPatients()

}