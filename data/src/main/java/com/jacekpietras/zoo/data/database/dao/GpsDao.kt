package com.jacekpietras.zoo.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacekpietras.zoo.data.database.model.GpsHistoryDto
import kotlinx.coroutines.flow.Flow

@Dao
internal interface GpsDao {

    @Query("SELECT * FROM gps_history ORDER BY timestamp")
    fun getAll(): List<GpsHistoryDto>

    @Query("SELECT * FROM gps_history ORDER BY timestamp")
    fun observeAll(): Flow<List<GpsHistoryDto>>

    @Query("SELECT * FROM gps_history ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(): Flow<GpsHistoryDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(position: GpsHistoryDto)
}