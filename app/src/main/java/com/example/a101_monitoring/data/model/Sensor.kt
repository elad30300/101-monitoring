package com.example.a101_monitoring.data.model

import androidx.room.ColumnInfo

data class Sensor(
    val address: String,
    @ColumnInfo(name = "is_scanning") var isScanning: Boolean = false,
    @ColumnInfo(name = "is_connecting") var isConnecting: Boolean = false,
    @ColumnInfo(name = "is_connected") var isConnected: Boolean = false
) {
}