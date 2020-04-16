package com.example.a101_monitoring.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.a101_monitoring.data.model.ReleaseReason

@Dao
interface ReleaseReasonsDao {

    @Query("SELECT * FROM release_reasons")
    fun getReleaseReasons(): LiveData<List<ReleaseReason>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg releaseReason: ReleaseReason)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(releaseReasons: List<ReleaseReason>)

    @Update
    fun update(vararg releaseReason: ReleaseReason)

    @Delete
    fun delete(vararg releaseReason: ReleaseReason)

}