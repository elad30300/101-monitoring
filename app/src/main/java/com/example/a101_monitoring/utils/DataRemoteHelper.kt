package com.example.a101_monitoring.utils

import com.example.a101_monitoring.data.model.Department
import com.example.a101_monitoring.data.model.DepartmentWithRooms
import com.example.a101_monitoring.data.model.Room
import com.example.a101_monitoring.remote.model.DepartmentBody

object DataRemoteHelper {

    fun fromRemoteToDataDepartment(departmentBody: DepartmentBody) = Department(departmentBody.id, departmentBody.name)

    fun fromRemoteToDataDepartmentWithRooms(departmentBody: DepartmentBody): DepartmentWithRooms {
        val rooms = departmentBody.rooms.map { Room(it, departmentBody.id) }
        return DepartmentWithRooms(fromRemoteToDataDepartment(departmentBody), rooms)
    }

    fun fromRemoteToDataListDepartmentWithRooms(departments: List<DepartmentBody>): List<DepartmentWithRooms> = departments.map { fromRemoteToDataDepartmentWithRooms(it) }

    fun fromRemoteToDataDepartments(departments: List<DepartmentBody>) = departments.map { fromRemoteToDataDepartment(it) }

}