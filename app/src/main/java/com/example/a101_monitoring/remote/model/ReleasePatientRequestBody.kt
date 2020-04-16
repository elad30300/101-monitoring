package com.example.a101_monitoring.remote.model

import com.example.a101_monitoring.data.model.PatientIdentityFieldType
import com.google.gson.annotations.SerializedName

data class ReleasePatientRequestBody (
    @SerializedName("PatientId") val patientId: Int,
    @SerializedName("DepartmentId") val departmentId: Int,
    @SerializedName("ReleaseReasonId") val releaseReasonId: Int
)