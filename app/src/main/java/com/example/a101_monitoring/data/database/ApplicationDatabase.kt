package com.example.a101_monitoring.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.a101_monitoring.data.dao.MeasurementsDao
import com.example.a101_monitoring.data.dao.PatientDao
import com.example.a101_monitoring.data.model.HeartRate
import com.example.a101_monitoring.data.model.Patient
import com.example.a101_monitoring.data.model.RespiratoryRate
import com.example.a101_monitoring.data.model.Saturation

@Database(entities = [
    Patient::class,
    HeartRate::class,
    Saturation::class,
    RespiratoryRate::class
], version = 1)
abstract class ApplicationDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao

    abstract fun measurementsDao(): MeasurementsDao

}