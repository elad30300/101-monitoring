package com.example.a101_monitoring.repository

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.a101_monitoring.data.dao.PatientDao
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.data.model.Sensor
import java.lang.Exception
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepository @Inject constructor(private val patientDao: PatientDao) {

    private val executor = Executors.newSingleThreadExecutor()

    private val fetchPatientsState = MutableLiveData<Boolean>()
    private val registerPatientState = MutableLiveData<Boolean>()

    fun getFetchPatientsState(): LiveData<Boolean> = fetchPatientsState
    fun getRegisterPatientState(): LiveData<Boolean> = registerPatientState

//    fun peformExceptionalBlock(state: MutableLiveData<Boolean>, block: () -> Any?): Any? {
//        try {
//            val result = block()
//            state.postValue(true)
//            return result
//        } catch (ex: Exception) {
//            state.postValue(false)
//            return null
//        }
//    }

    fun getPatients() = patientDao.getAll()

    fun isPatientConnectedToSensor(patientId: Int) = patientDao.isPatientConnectedToSensor(patientId)

    fun registerPatient(patient: Patient) {
        executor.execute {
            try {
                patientDao.insertPatients(patient)
            } catch (ex: Exception) {
                registerPatientState.postValue(false)
            }
        }
    }

}