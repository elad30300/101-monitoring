package com.example.a101_monitoring.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.a101_monitoring.MyApplication
import com.example.a101_monitoring.data.model.HeartRate
import com.example.a101_monitoring.data.model.PatientIdentityFieldType
import com.example.a101_monitoring.data.model.RespiratoryRate
import com.example.a101_monitoring.data.model.Saturation
import com.example.a101_monitoring.repository.MeasurementsRepository
import com.example.a101_monitoring.repository.PatientRepository
import javax.inject.Inject


class PatientItemViewModel(
    private val context: Context,
    private val patientId: PatientIdentityFieldType
): ViewModel() {

    @Inject lateinit var patientRepository: PatientRepository
    @Inject lateinit var measurementsRepository: MeasurementsRepository

    var isPatientConnectedToSensor: LiveData<Boolean?>

    val heartRate: LiveData<HeartRate?>
    val saturation: LiveData<Saturation?>
    val respiratoryRate: LiveData<List<RespiratoryRate>>

    init {
        (context.applicationContext as MyApplication).applicationComponent.patientItemComponent().create().also {
            it.inject(this)
        }

        isPatientConnectedToSensor = patientRepository.isPatientConnectedToSensor(patientId)
        heartRate = measurementsRepository.getLastHeartRateForPatient(patientId)
        saturation = measurementsRepository.getLastSaturationForPatient(patientId)
        respiratoryRate = measurementsRepository.getLastRespiratoryRateForPatient(patientId)
    }
}