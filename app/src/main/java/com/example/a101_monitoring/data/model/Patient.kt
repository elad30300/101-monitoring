package com.example.a101_monitoring.data.model

import androidx.annotation.Nullable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey val numericalId: String,
    val id: Int,
    val deptId: Int,
    val room: String,
    val bed: String,
    val haitiId: String,
    val registeredDoctor: String,
    val isCitizen: Boolean,
    val isOxygen: Int,
    val isActive: Boolean,
    @Embedded var sensor: Sensor? = null
)