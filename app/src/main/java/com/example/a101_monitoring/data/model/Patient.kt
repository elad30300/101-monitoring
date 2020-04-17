package com.example.a101_monitoring.data.model

import androidx.annotation.Nullable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

typealias PatientIdentityFieldType = String

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey val identityId: String,
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
) {
    fun getIdentityField() = identityId

    override fun equals(other: Any?): Boolean {
        if (other == null || !(other!! is Patient)) {
            return false
        }
        val otherPatient = other as Patient
        return (
                id == otherPatient.id
                        && deptId == otherPatient.deptId
                        && room == otherPatient.room
                        && bed == otherPatient.bed
                        && haitiId == otherPatient.haitiId
                        && registeredDoctor == otherPatient.registeredDoctor
//                        && isCitizen == otherPatient.isCitizen
//                        && id == otherPatient.id
                )
    }

    companion object {
        const val IDENTITY_FIELD_IN_DB = "identityId"
    }
}