package com.example.a101_monitoring.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.a101_monitoring.data.dao.PatientDao
import com.example.a101_monitoring.data.model.Patient
import javax.inject.Inject

@Database(entities = [Patient::class], version = 1)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
}