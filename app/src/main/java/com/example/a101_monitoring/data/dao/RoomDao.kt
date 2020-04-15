package com.example.a101_monitoring.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.a101_monitoring.data.model.Room

@Dao
interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg room: Room)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rooms: List<Room>)

}