package com.example.a101_monitoring.remote.model

import com.google.gson.annotations.SerializedName

open class ManualMeasurementBody (
    @SerializedName("PatientId") val patientId: Int,
    @SerializedName("DepartmentId") val departmentId: Int,
    @SerializedName("data") val data: String,
    @SerializedName("timestamp") val timestamp: Long
)

class BloodPressureBody(
    patientId: Int,
    departmentId: Int,
    diastolic: Int,
    systolic: Int,
    timestamp: Long
) : ManualMeasurementBody(patientId, departmentId, "$systolic/$diastolic", timestamp)

class BodyTemperatureBody(
    patientId: Int,
    departmentId: Int,
    value: Float,
    timestamp: Long
) : ManualMeasurementBody(patientId, departmentId, value.toString(), timestamp)