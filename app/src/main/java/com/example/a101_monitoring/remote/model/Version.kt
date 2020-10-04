package com.example.a101_monitoring.remote.model

import com.google.gson.annotations.SerializedName

data class Version(@SerializedName("phoneId") private val phoneId: String,
                   @SerializedName("number") private val phoneNumber: String,
                   @SerializedName("version") private val version: String)