package com.example.a101_monitoring.repository

import android.util.Log
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

    fun getSensorAddress(patientId: Int) = patientDao.getSensorAddress(patientId)

    fun getSensors() = patientDao.getAllSensors()

    fun registerPatient(patient: Patient) {
        executor.execute {
            try {
                patientDao.insertPatients(patient)
                Log.d(TAG, "insert patient with id ${patient.id} in dao successfully")
            } catch (ex: Exception) {
                Log.e(TAG, "insert patient with id ${patient.id} in dao failed, stacktrace:")
                ex.printStackTrace()
                registerPatientState.postValue(false)
            }
        }
    }

    fun setSensor(patientId: Int, sensorAddress: String) {
        executor.execute {
            try {
                patientDao.updateSensorToPatient(patientId, sensorAddress)
                Log.d(TAG, "Set sensor with address $sensorAddress to patient with id $patientId in dao successfully")
            } catch (ex: Exception) {
                Log.e(TAG, "Set sensor with address $sensorAddress to patient with id $patientId in dao failed, stacktrace:")
                ex.printStackTrace()
//                registerPatientState.postValue(false)
            }
        }
    }

    fun setSensorIsConnected(sensorAddress: String, isConnected: Boolean) {
        executor.execute {
            try {
                patientDao.setSensorIsConnected(sensorAddress, isConnected)
                Log.d(TAG, "Set sensor is connected to ${isConnected} with address $sensorAddress in dao successfully")
            } catch (ex: Exception) {
                Log.e(TAG, "Set sensor is connected to ${isConnected} with address $sensorAddress in dao failed, stacktrace:")
                ex.printStackTrace()
//                registerPatientState.postValue(false)
            }
        }
    }

    companion object {
        const val TAG = "PatientRepository"
    }

}