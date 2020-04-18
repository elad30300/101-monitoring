package com.example.a101_monitoring.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.a101_monitoring.data.dao.DepartmentDao
import com.example.a101_monitoring.data.dao.PatientDao
import com.example.a101_monitoring.data.dao.RoomDao
import com.example.a101_monitoring.data.model.*
import com.example.a101_monitoring.remote.adapter.AtalefRemoteAdapter
import com.example.a101_monitoring.remote.adapter.OnErrorCallback
import com.example.a101_monitoring.remote.adapter.OnResponseCallback
import com.example.a101_monitoring.remote.adapter.OnFailedCallback
import com.example.a101_monitoring.remote.model.DepartmentBody
import com.example.a101_monitoring.remote.model.PatientBody
import com.example.a101_monitoring.remote.model.PatientSignInBody
import com.example.a101_monitoring.remote.model.ReleasePatientRequestBody
import com.example.a101_monitoring.states.*
import com.example.a101_monitoring.utils.DataRemoteHelper
import com.example.a101_monitoring.utils.DefaultCallbacksHelper
import com.example.a101_monitoring.utils.ExceptionsHelper
import java.lang.Exception
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepository @Inject constructor(
    private val patientDao: PatientDao,
    private val departmentDao: DepartmentDao,
    private val roomDao: RoomDao,
    private val atalefRemoteAdapter: AtalefRemoteAdapter,
    private val executor: Executor
) {

    private val registerPatientState = MutableLiveData<RegisterPatientState>()
    private val signInPatientState = MutableLiveData<SignInPatientState>()
    private val submitSensorToPatientState = MutableLiveData<SubmitSensorToPatientState>()

    private val availableBeds = MutableLiveData<List<String>>()

    fun getRegisterPatientState(): LiveData<RegisterPatientState> = registerPatientState
    fun getSignInPatientState(): LiveData<SignInPatientState> = signInPatientState
    fun getSubmitSensorToPatientState(): LiveData<SubmitSensorToPatientState> = submitSensorToPatientState

    fun getPatients() = patientDao.getAll()

    fun getPatientIdBySensorAddress(address: String) = patientDao.getPatientIdBySensorAddress(address)

    fun isPatientConnectedToSensor(patientId: PatientIdentityFieldType) = patientDao.isPatientConnectedToSensor(patientId)

    fun getSensorAddress(patientId: PatientIdentityFieldType) = patientDao.getSensorAddress(patientId)

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
            atalefRemoteAdapter.getAvailableBeds(room.name, room.departmentId, {
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

    fun registerPatient(identityNumber: String, deptId: Int, room: String, bed: String, haitiId: String, registeredDoctor: String,
                        isCitizen: Boolean, isOxygen: Int, isActive: Boolean, sensorAddress: String = "") {
        val patientBody = PatientBody(0, identityNumber, deptId, room, bed, haitiId, registeredDoctor, isCitizen, isOxygen, isActive)
        registerPatientState.postValue(RegisterPatientWorkingState())
        registerPatientToRemote(patientBody,
            {
                onPatientRegisteredSuccessfullyToRemote(it, sensorAddress)
            }, {
                DefaultCallbacksHelper.onErrorDefault(TAG, "Failure: register patient ${identityNumber} to remote failed", it)
                registerPatientState.postValue(RegisterPatientFailedState())
            }, {
                DefaultCallbacksHelper.onErrorDefault(TAG, "Error: register patient ${identityNumber} to remote failed", it)
                registerPatientState.postValue(RegisterPatientFailedState())
            }
        )
    }

    private fun registerPatientToRemote(patientBody: PatientBody, onResponseCallback: OnResponseCallback<PatientBody>, onFailed: OnFailedCallback, onError: OnErrorCallback) {
        executor.execute {
            atalefRemoteAdapter.register(patientBody, onResponseCallback, onFailed, onError)
        }
    }

    private fun onPatientRegisteredSuccessfullyToRemote(patientBody: PatientBody, sensorAddress: String = "") {
        val patient = DataRemoteHelper.fromPatientBodyToPatient(patientBody).apply {
            sensor = Sensor(sensorAddress)
        }
        insertPatient(patient, {
            DefaultCallbacksHelper.onSuccessDefault(TAG, "insert patient with id ${patient.id} in dao successfully")
            registerPatientState.postValue(RegisterPatientDoneState())
        }, {
            DefaultCallbacksHelper.onErrorDefault(TAG, "insert patient with id ${patient.id} in dao failed", it)
            registerPatientState.postValue(RegisterPatientFailedState())
        })
    }

    fun signIn(patientId: PatientIdentityFieldType) {
        signInPatientState.postValue(SignInPatientWorkingState())
        executor.execute {
            atalefRemoteAdapter.signIn(PatientSignInBody(patientId),
                {
                    onSignInResponse(it)
                }, {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "Failure: sign in request for patient ${patientId} to remote failed - patient not exist", it)
                    signInPatientState.postValue(SignInPatientFailedState())
                }, {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "Error: sign in request for patient ${patientId} to remote failed", it)
                    signInPatientState.postValue(SignInPatientFailedState())
                }
            )
        }
    }

    private fun onSignInResponse(patientBody: PatientBody) {
        val patientFromRemote = DataRemoteHelper.fromPatientBodyToPatient(patientBody)
        insertPatient(patientFromRemote, {
            DefaultCallbacksHelper.onSuccessDefault(TAG, "sign in patient with id ${it.id} in dao successfully")
            signInPatientState.postValue(SignInPatientDoneState())
        }, {
            DefaultCallbacksHelper.onErrorDefault(TAG, "sign in patient with id ${patientBody.id} in dao failed", it)
            signInPatientState.postValue(SignInPatientFailedState())
        })
    }

    private fun insertPatient(patient: Patient, onPatientInsert: (patient: Patient) -> Unit, onFail: (throwable: Throwable) -> Unit) {
        executor.execute {
            try {
                patientDao.insertPatients(patient)
                onPatientInsert(patient)
            } catch (ex: Exception) {
                onFail(ex)
            }
        }
    }

    fun setSensor(patientId: PatientIdentityFieldType, sensorAddress: String) {
        submitSensorToPatientState.postValue(SubmitSensorToPatientWorkingState())
        executor.execute {
            try {
                patientDao.updateSensorToPatient(patientId, sensorAddress)
                Log.d(TAG, "Set sensor with address $sensorAddress to patient with id $patientId in dao successfully")
                submitSensorToPatientState.postValue(SubmitSensorToPatientDoneState())
            } catch (ex: Exception) {
                Log.e(TAG, "Set sensor with address $sensorAddress to patient with id $patientId in dao failed, stacktrace:")
                ex.printStackTrace()
                submitSensorToPatientState.postValue(SubmitSensorToPatientFailedState())
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

    fun releasePatient(patientId: PatientIdentityFieldType, releaseReason: ReleaseReason) {
        executor.execute {
            val patient = patientDao.getPatient(patientId)
            atalefRemoteAdapter.releasePatient(
                ReleasePatientRequestBody(patient.id, patient.deptId, releaseReason.id),
                {
                    Log.i(TAG, "patient $patientId was successfully released from remote")
                    onPatientReleased(patient)
                }, {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "failure in release patient $patientId from remote", it)
                }, {
                    DefaultCallbacksHelper.onErrorDefault(TAG, "error in release patient $patientId from remote", it)
                }
            )
        }
    }

    private fun onPatientReleased(patient: Patient) {
        executor.execute {
            ExceptionsHelper.tryBlock(TAG, "delete patient ${patient.getIdentityField()} from database") {
                patientDao.deletePatients(patient)
            }
        }
    }

    companion object {
        const val TAG = "PatientRepository"
        val FRESH_TIMEOUT = TimeUnit.DAYS.toMillis(1)
    }

}