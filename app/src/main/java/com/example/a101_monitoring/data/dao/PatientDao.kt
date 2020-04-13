package com.example.a101_monitoring.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.data.model.Sensor

@Dao
interface PatientDao {

    @Query("SELECT * FROM patients")
    fun getAll(): LiveData<List<Patient>>

    @Query("SELECT address FROM patients WHERE id = :patientId")
    fun getSensorAddress(patientId: Int): LiveData<String?>

    @Query("SELECT address, is_connected FROM patients")
    fun getAllSensors(): LiveData<List<Sensor>>

    @Query("UPDATE patients SET address = :sensorAddress , is_connected = :isConnected WHERE id = :patientId")
    fun updateSensorToPatient(patientId: Int, sensorAddress: String, isConnected: Boolean = false)

    @Query("UPDATE patients SET is_connected = :isConnected WHERE address = :sensorAddress")
    fun setSensorIsConnected(sensorAddress: String, isConnected: Boolean)

    @Query("SELECT is_connected FROM patients WHERE id = :patientId")
    fun isPatientConnectedToSensor(patientId: Int): LiveData<Boolean?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPatients(vararg patients: Patient)

    @Update
    fun updatePatients(vararg patients: Patient)

    @Delete
    fun deletePatients(vararg patients: Patient)

}