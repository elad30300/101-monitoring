package com.example.a101_monitoring.remote.model

import com.google.gson.annotations.SerializedName

class ReleaseReasonBody (
    @SerializedName("id") val id: Int,
    @SerializedName("description") val description: String
)