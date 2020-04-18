package com.example.a101_monitoring.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.a101_monitoring.data.model.Department
import com.example.a101_monitoring.data.model.DepartmentWithRooms

@Dao
interface DepartmentDao {

    @Transaction
    @Query("SELECT * FROM departments")
    fun getAll(): LiveData<List<DepartmentWithRooms>>

    @Transaction
    @Query("SELECT * FROM departments WHERE id = :id")
    fun get(id: Int): DepartmentWithRooms

    @Query("DELETE FROM departments")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(departments: List<Department>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg department: Department)

    @Update
    fun update(vararg department: Department)

    @Update
    fun update(departments: List<Department>)

    @Delete
    fun delete(vararg department: Department)

    @Delete
    fun delete(departments: List<Department>)

}