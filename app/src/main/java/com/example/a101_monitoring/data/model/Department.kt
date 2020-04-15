package com.example.a101_monitoring.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "departments")
data class Department (
     @PrimaryKey val id: Int,
     val name: String
)

data class DepartmentWithRooms(
     @Embedded val department: Department,
     @Relation(
          parentColumn = "id",
          entityColumn = "departmentId"
     )
     val rooms: List<Room>
)