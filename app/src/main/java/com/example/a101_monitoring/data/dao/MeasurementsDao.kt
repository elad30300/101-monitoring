package com.example.a101_monitoring.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.a101_monitoring.data.model.HeartRate
import com.example.a101_monitoring.data.model.PatientIdentityFieldType
import com.example.a101_monitoring.data.model.RespiratoryRate
import com.example.a101_monitoring.data.model.Saturation

@Dao
interface MeasurementsDao {

    @Query("SELECT * FROM heart_rates")
    fun getAllHeartRates(): LiveData<List<HeartRate>>

    @Query("SELECT * FROM saturations")
    fun getAllSaturations(): LiveData<List<Saturation>>

    @Query("SELECT * FROM respirations")
    fun getAllRespiratoryRates(): LiveData<List<RespiratoryRate>>

    @Query("SELECT * FROM heart_rates WHERE patient_id = :patientId")
    fun getAllHeartRatesForPatient(patientId: PatientIdentityFieldType): LiveData<HeartRate>

    @Query("SELECT * FROM saturations WHERE patient_id = :patientId")
    fun getAllSaturationsForPatient(patientId: PatientIdentityFieldType): LiveData<Saturation>

    @Query("SELECT * FROM respirations WHERE patient_id = :patientId")
    fun getAllRespiratoryRatesForPatient(patientId: PatientIdentityFieldType): LiveData<RespiratoryRate>

    @Query("""
        SELECT *
        FROM heart_rates
        WHERE patient_id = :patientId AND time = (
            SELECT MAX(time)
            FROM heart_rates
        )
        """)
    fun getLastHeartRateForPatient(patientId: PatientIdentityFieldType): LiveData<HeartRate?>

    @Query("""
        SELECT *
        FROM saturations
        WHERE patient_id = :patientId AND time = (
            SELECT MAX(time)
            FROM saturations
        )
        """)
    fun getLastSaturationForPatient(patientId: PatientIdentityFieldType): LiveData<Saturation?>

    @Query("""
        SELECT *
        FROM respirations
        WHERE patient_id = :patientId AND time = (
            SELECT MAX(time)
            FROM respirations
        )
        """)
    fun getLastRespiratoryRateForPatient(patientId: PatientIdentityFieldType): LiveData<List<RespiratoryRate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHeartRates(vararg heartRate: HeartRate)

    @Update
    fun updateHeartRates(vararg heartRate: HeartRate)

    @Delete
    fun deleteHeartRates(vararg heartRate: HeartRate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSaturations(vararg saturation: Saturation)

    @Update
    fun updateSaturations(vararg saturation: Saturation)

    @Delete
    fun deleteSaturations(vararg saturation: Saturation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRespiratoryRates(vararg respiratoryRate: RespiratoryRate)

    @Update
    fun updateRespiratoryRates(vararg respiratoryRate: RespiratoryRate)

    @Delete
    fun deleteRespiratoryRates(vararg hrespiratoryRate: RespiratoryRate)

}