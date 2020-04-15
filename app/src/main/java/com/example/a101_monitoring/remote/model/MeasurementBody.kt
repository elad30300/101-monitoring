package com.example.a101_monitoring.remote.model

import com.google.gson.annotations.SerializedName

data class MeasurementBody (
    @SerializedName("patientId") private val patientId: Int,
    @SerializedName("DepartmentId") private val departmentId: Int,
    @SerializedName("timestamp") private val timestamp: Long,
    @SerializedName("heartbeat") private val heartBeat: String,
    @SerializedName("saturation") private val saturation: String,
    @SerializedName("breathing") private val breathing: String
)