package com.example.a101_monitoring.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

open class Measurement(
    open val value: Int,
    open val time: Long,
    @ColumnInfo(name = "patient_id") open val patientId: String
) {
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "measurment_id") val measurmentId: Int = 0
}

@Entity(tableName = "heart_rates")
data class HeartRate(
    override val value: Int,
    override val time: Long,
    @ColumnInfo(name = "patient_id") override val patientId: String
) : Measurement(value, time, patientId)


@Entity(tableName = "saturations")
data class Saturation(
    override val value: Int,
    override val time: Long,
    @ColumnInfo(name = "patient_id") override val patientId: String
) : Measurement(value, time, patientId)

@Entity(tableName = "respirations")
data class RespiratoryRate(
    override val value: Int,
    override val time: Long,
    @ColumnInfo(name = "patient_id") override val patientId: String
) : Measurement(value, time, patientId)