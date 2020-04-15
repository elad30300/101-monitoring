package com.example.a101_monitoring.remote.model

import com.google.gson.annotations.SerializedName

class DepartmentBody (@SerializedName("id") val id: Int,
                      @SerializedName("rooms") val rooms: Array<String>,
                      @SerializedName("name") val name: String)