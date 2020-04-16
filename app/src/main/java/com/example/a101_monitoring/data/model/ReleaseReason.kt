package com.example.a101_monitoring.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "release_reasons")
data class ReleaseReason (
    @PrimaryKey val id: Int,
    val description: String
)