package com.example.a101_monitoring.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.data.model.PatientIdentityFieldType
import com.example.a101_monitoring.data.model.Sensor

@Dao
interface PatientDao {

    @Query("SELECT * FROM patients")
    fun getAll(): LiveData<List<Patient>>

    @Query("SELECT * FROM patients WHERE ${Patient.IDENTITY_FIELD_IN_DB} = :patientId")
    fun getPatient(patientId: PatientIdentityFieldType): Patient

    @Query("SELECT ${Patient.IDENTITY_FIELD_IN_DB} FROM patients WHERE address = :address")
    fun getPatientIdBySensorAddress(address: String): PatientIdentityFieldType

    @Query("SELECT * FROM patients WHERE address = :address")
    fun getPatientBySensorAddress(address: String): Patient

    @Query("SELECT address FROM patients WHERE ${Patient.IDENTITY_FIELD_IN_DB} = :patientId")
    fun getSensorAddress(patientId: PatientIdentityFieldType): LiveData<String?>

    @Query("SELECT address, is_connected FROM patients")
    fun getAllSensors(): LiveData<List<Sensor>>

    @Query("UPDATE patients SET address = :sensorAddress , is_connected = :isConnected WHERE ${Patient.IDENTITY_FIELD_IN_DB} = :patientId")
    fun updateSensorToPatient(patientId: PatientIdentityFieldType, sensorAddress: String, isConnected: Boolean = false)

    @Query("UPDATE patients SET is_connected = :isConnected WHERE address = :sensorAddress")
    fun setSensorIsConnected(sensorAddress: String, isConnected: Boolean)

    @Query("UPDATE patients SET is_connected = :isConnected")
    fun setAllSensorsIsConnected(isConnected: Boolean)

    @Query("SELECT is_connected FROM patients WHERE ${Patient.IDENTITY_FIELD_IN_DB} = :patientId")
    fun isPatientConnectedToSensor(patientId: PatientIdentityFieldType): LiveData<Boolean?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPatients(vararg patients: Patient)

    @Update
    fun updatePatients(vararg patients: Patient)

    @Delete
    fun deletePatients(vararg patients: Patient)

}