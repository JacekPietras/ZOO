package com.jacekpietras.zoo.data.database.dao

import androidx.room.*
import com.jacekpietras.zoo.data.database.model.GpsHistoryDto
import kotlinx.coroutines.flow.Flow

@Dao
internal interface GpsDao {

    @Query("SELECT * FROM gps_history")
    fun getAll(): List<GpsHistoryDto>

    @Query("SELECT * FROM gps_history ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(): Flow<GpsHistoryDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(position: GpsHistoryDto)
}