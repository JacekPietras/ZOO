package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import kotlinx.coroutines.flow.Flow

interface GpsRepository {

    fun observeLatestPosition(): Flow<GpsHistoryEntity>

    fun observeAllPositions(): Flow<List<List<GpsHistoryEntity>>>

    fun observeOldPositions(): Flow<List<List<GpsHistoryEntity>>>

    suspend fun getAllPositions(): List<GpsHistoryEntity>

    suspend fun getAllPositionsNormalized(): List<List<GpsHistoryEntity>>

    suspend fun insertPosition(position: GpsHistoryEntity)

    fun observeCompass(): Flow<Float>

    fun insertCompass(angle: Float)

    fun enableCompass()

    fun disableCompass()

    fun observeCompassEnabled(): Flow<Boolean>

    fun insertLuminance(luminance: Float)

    fun observeLuminance(): Flow<Float>

    fun enableLightSensor()

    fun disableLightSensor()

    fun observeLightSensorEnabled(): Flow<Boolean>

    fun enableNavigation()

    fun disableNavigation()

    fun observeNavigationEnabled(): Flow<Boolean>
}