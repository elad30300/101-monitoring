package com.example.a101_monitoring.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(tableName = "rooms", primaryKeys = ["name", "departmentId"])
data class Room (
    val name: String,
    @ForeignKey(entity = Department::class,
        parentColumns = ["id"],
        onDelete = ForeignKey.CASCADE,
        childColumns = ["departmentId"]) val departmentId: Int
)