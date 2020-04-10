package com.example.a101_monitoring.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.a101_monitoring.data.model.Patient

@Dao
interface PatientDao {

    @Query("SELECT * FROM patients")
    fun getAll(): LiveData<List<Patient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPatients(vararg patients: Patient)

    @Update
    fun updatePatients(vararg patients: Patient)

    @Delete
    fun deletePatients(vararg patients: Patient)

}