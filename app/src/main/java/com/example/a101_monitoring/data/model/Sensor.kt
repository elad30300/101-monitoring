package com.example.a101_monitoring.data.model

import androidx.room.ColumnInfo

data class Sensor(
    val address: String,
    @ColumnInfo(name = "is_connected") var isConnected: Boolean = false
) {
}