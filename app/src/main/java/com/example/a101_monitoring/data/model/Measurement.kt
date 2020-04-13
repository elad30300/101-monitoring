package com.example.a101_monitoring.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

open class Measurement(
    @ColumnInfo(name = "value") val value: Int,
    @ColumnInfo(name = "time") val time: Long,
    @ColumnInfo(name = "patient_id") val patientId: Int
) {
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "measurement_id") var measurementId: Int = 0

    override fun toString(): String {
        return "[${this.javaClass::class.java.name} - value: $value,  time: $time, patientId: $patientId]"
    }
}

@Entity(tableName = "heart_rates")
class HeartRate(
    value: Int,
    time: Long,
    patientId: Int
) : Measurement(value, time, patientId)


@Entity(tableName = "saturations")
class Saturation(
    value: Int,
    time: Long,
    patientId: Int
) : Measurement(value, time, patientId)

@Entity(tableName = "respirations")
class RespiratoryRate(
    value: Int,
    time: Long,
    patientId: Int
) : Measurement(value, time, patientId)