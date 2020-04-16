package com.example.a101_monitoring.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.a101_monitoring.data.dao.*
import com.example.a101_monitoring.data.model.*

@Database(entities = [
    Patient::class,
    HeartRate::class,
    Saturation::class,
    RespiratoryRate::class,
    Department::class,
    Room::class,
    ReleaseReason::class
], version = 1, exportSchema = false)
abstract class ApplicationDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao

    abstract fun measurementsDao(): MeasurementsDao

    abstract fun departmentDao(): DepartmentDao

    abstract fun roomDao(): RoomDao

    abstract fun releaseReasonsDao(): ReleaseReasonsDao

//    companion object {
//        val migration1to2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL(
//                    "CREATE TABLE 'departments' ('id' INTEGER NOT NULL, 'name' TEXT NOT NULL, PRIMARY KEY('id'))"
//                )
//            }
//        }
//    }

}