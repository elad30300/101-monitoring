package com.example.a101_monitoring.remote.model

import com.google.gson.annotations.SerializedName

data class PatientBody (
    @SerializedName("id") val id: Int,
    @SerializedName("identityNumber") val identityNumber: String,
    @SerializedName("DepartmentId") val deptId: Int,
    @SerializedName("room") val room: String,
    @SerializedName("bed") val bed: String,
    @SerializedName("haitiId") var haitiId: String,
    @SerializedName("registeredDoctor") val registeredDoctor: String,
    @SerializedName("isCitizen") val isCitizen: Boolean,
    @SerializedName("isOxygen") val isOxygen: Int,
    @SerializedName("isActive") val isActive: Boolean
)

data class PatientIdBody(@SerializedName("patientId") val patientId: Int)

data class PatientSignInBody(@SerializedName("patientIdentityNumber") val patientIdentityNumber: String)
