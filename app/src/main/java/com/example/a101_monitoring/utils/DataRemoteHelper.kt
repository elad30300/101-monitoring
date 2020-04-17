package com.example.a101_monitoring.utils

import com.example.a101_monitoring.data.model.*
import com.example.a101_monitoring.remote.model.DepartmentBody
import com.example.a101_monitoring.remote.model.MeasurementBody
import com.example.a101_monitoring.remote.model.PatientBody
import com.example.a101_monitoring.remote.model.ReleaseReasonBody

object DataRemoteHelper {

    fun fromRemoteToDataDepartment(departmentBody: DepartmentBody) = Department(departmentBody.id, departmentBody.name)

    fun fromRemoteToDataDepartmentWithRooms(departmentBody: DepartmentBody): DepartmentWithRooms {
        val rooms = departmentBody.rooms.map { Room(it, departmentBody.id) }
        return DepartmentWithRooms(fromRemoteToDataDepartment(departmentBody), rooms)
    }

    fun fromRemoteToDataListDepartmentWithRooms(departments: List<DepartmentBody>): List<DepartmentWithRooms> = departments.map { fromRemoteToDataDepartmentWithRooms(it) }

    fun fromRemoteToDataDepartments(departments: List<DepartmentBody>) = departments.map { fromRemoteToDataDepartment(it) }

    fun fromPatientBodyToPatient(patientBody: PatientBody) = patientBody.run {
        Patient(identityNumber, id, deptId, room, bed, haitiId, registeredDoctor, isCitizen, isOxygen, isActive)
    }

    fun fromRemoteToDataReleaseReason(releaseReasonBody: ReleaseReasonBody) = ReleaseReason(releaseReasonBody.id, releaseReasonBody.description)

    fun fromRemoteToDataReleaseReasonList(releaseReasonBodies: List<ReleaseReasonBody>) = releaseReasonBodies.map { fromRemoteToDataReleaseReason(it) }

    fun fromDataToRemoteMeasurements(heartRate: HeartRate?, saturation: Saturation?, respiratoryRate: RespiratoryRate?, patient: Patient): MeasurementBody {
        val time = (heartRate?.time ?: saturation?.time ?: respiratoryRate?.time)!!
        return MeasurementBody(
            patient.id,
            patient.deptId,
            time,
            (heartRate?.value ?: MeasurementBody.HEART_BEAT_MISSING_VALUE).toString(),
            (saturation?.value ?: MeasurementBody.SATURATION_MISSING_VALUE).toString(),
            (respiratoryRate?.value ?: MeasurementBody.BREATHING_MISSING_VALUE).toString()
        )
    }

}