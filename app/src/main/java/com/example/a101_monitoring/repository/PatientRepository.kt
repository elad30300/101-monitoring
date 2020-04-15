package com.example.a101_monitoring.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.a101_monitoring.data.dao.DepartmentDao
import com.example.a101_monitoring.data.dao.PatientDao
import com.example.a101_monitoring.data.dao.RoomDao
import com.example.a101_monitoring.data.model.*
import com.example.a101_monitoring.remote.adapter.AtalefRemoteAdapter
import com.example.a101_monitoring.remote.adapter.RetrofitAtalefRemoteAdapter
import com.example.a101_monitoring.remote.model.DepartmentBody
import com.example.a101_monitoring.utils.DataRemoteHelper
import com.example.a101_monitoring.utils.DefaultCallbacksHelper
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepository @Inject constructor(
    private val patientDao: PatientDao,
    private val departmentDao: DepartmentDao,
    private val roomDao: RoomDao,
    private val atalefRemoteAdapter: AtalefRemoteAdapter
) {

    private val executor = Executors.newSingleThreadExecutor()

    private val fetchPatientsState = MutableLiveData<Boolean>()
    private val registerPatientState = MutableLiveData<Boolean>()

    private val availableBeds = MutableLiveData<List<String>>()

    fun getFetchPatientsState(): LiveData<Boolean> = fetchPatientsState
    fun getRegisterPatientState(): LiveData<Boolean> = registerPatientState

    fun getPatients() = patientDao.getAll()

    fun getPatientIdBySensorAddress(address: String) = patientDao.getPatientIdBySensorAddress(address)

    fun isPatientConnectedToSensor(patientId: Int) = patientDao.isPatientConnectedToSensor(patientId)

    fun getSensorAddress(patientId: Int) = patientDao.getSensorAddress(patientId)

    fun getSensors() = patientDao.getAllSensors()

    fun getDepartments(): LiveData<List<DepartmentWithRooms>> {
        getDepartmentsFromRemote()
        return departmentDao.getAll()
    }

    fun getAvailableBeds(): LiveData<List<String>> = availableBeds

    fun updateAvailableBeds(room: Room) {
        getAvailableBedsFromRemote(room)
    }

    private fun getAvailableBedsFromRemote(room: Room) {
        executor.execute {
            atalefRemoteAdapter.getAvailableBeds(room, {
                onGotAvailableBeds(it)
            }, {
                DefaultCallbacksHelper.onErrorDefault(TAG, "failed to fetch available beds for room ${room.name}, department ${room.departmentId} from remote", it)
            }, {
                DefaultCallbacksHelper.onErrorDefault(TAG, "error in fetch available beds for room ${room.name}, department ${room.departmentId} from remote", it)
            })
        }
    }

    private fun onGotAvailableBeds(beds: List<String>) {
        availableBeds.postValue(beds)
    }

    private fun getDepartmentsFromRemote() {
        executor.execute {
            atalefRemoteAdapter.getDepartments(
                {
                    onGetDepartmentsFromRemote(it)
                }, {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "failed to fetch departments from remote", it)
                }, {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "error in fetch departments from remote", it)
                }
            )
        }
    }

    private fun onGetDepartmentsFromRemote(departments: List<DepartmentBody>) {
        // TODO change that to more efficient refresh - delete non-existing departments and update only updated departments
        deleteAllDepartments()
        val departmentsWithRooms = DataRemoteHelper.fromRemoteToDataListDepartmentWithRooms(departments)
        departmentsWithRooms.forEach {
            insertDepartment(it.department)
            insertRooms(it.rooms)
        }
    }

    private fun deleteAllDepartments() {
        executor.execute {
            try {
                // TODO change that to more efficient refresh - delete non-existing departments and update only updated departments
                departmentDao.deleteAll()
            } catch (exception: Exception) {
                DefaultCallbacksHelper.onErrorDefault(TAG, "problem in deleting all departments", exception)
            }
        }
    }

    private fun insertDepartment(department: Department) {
        executor.execute {
            try {
                departmentDao.insert(department)
            } catch (exception: Exception) {
                DefaultCallbacksHelper.onErrorDefault(TAG, "problem in inserting departments (list)", exception)
            }
        }
    }

    private fun insertDepartments(departments: List<Department>) {
        executor.execute {
            try {
                departmentDao.insert(departments)
            } catch (exception: Exception) {
                DefaultCallbacksHelper.onErrorDefault(TAG, "problem in inserting departments (list)", exception)
            }
        }
    }

    private fun insertRooms(rooms: List<Room>) {
        executor.execute {
            try {
                roomDao.insert(rooms)
            } catch (exception: Exception) {
                DefaultCallbacksHelper.onErrorDefault(TAG, "problem in inserting rooms (list)", exception)
            }
        }
    }

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
        val FRESH_TIMEOUT = TimeUnit.DAYS.toMillis(1)
    }

}