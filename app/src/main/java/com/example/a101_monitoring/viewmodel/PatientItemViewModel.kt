package com.example.a101_monitoring.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.a101_monitoring.MyApplication
import com.example.a101_monitoring.repository.PatientRepository
import javax.inject.Inject


class PatientItemViewModel(
    private val context: Context,
    private val patientId: Int
): ViewModel() {

    @Inject lateinit var patientRepository: PatientRepository

    var isPatientConnectedToSensor: LiveData<Boolean?>

    init {
        (context.applicationContext as MyApplication).applicationComponent.patientItemComponent().create().also {
            it.inject(this)
        }

        isPatientConnectedToSensor = patientRepository.isPatientConnectedToSensor(patientId)
    }
}