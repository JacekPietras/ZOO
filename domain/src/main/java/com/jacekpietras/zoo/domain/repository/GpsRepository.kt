package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import kotlinx.coroutines.flow.Flow

interface GpsRepository {

    fun observeLatestPosition(): Flow<GpsHistoryEntity>

    suspend fun getAllPositions(): List<GpsHistoryEntity>

    suspend fun insertPosition(position: GpsHistoryEntity)
}