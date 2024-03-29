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
    suspend fun getAll(): List<GpsHistoryDto>

    @Query("SELECT * FROM gps_history ORDER BY timestamp")
    fun observeAll(): Flow<List<GpsHistoryDto>>

    @Query("SELECT * FROM gps_history ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<GpsHistoryDto>

    @Query("SELECT * FROM gps_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): GpsHistoryDto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(position: GpsHistoryDto)
}