package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.core.RectD
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.VisitedRoadPoint
import kotlinx.coroutines.flow.Flow

interface MapRepository {

    suspend fun observeWorldBounds(): Flow<RectD>

    fun getWorldBounds(): RectD

    suspend fun getCurrentRegions(): List<Pair<Region, PolygonEntity>>

    suspend fun observeBuildings(): Flow<List<PolygonEntity>>

    suspend fun observeAviary(): Flow<List<PolygonEntity>>

    suspend fun observeRoads(): Flow<List<PathEntity>>

    suspend fun getRoads(): List<PathEntity>

    suspend fun observeVisitedRoads(): Flow<List<List<VisitedRoadPoint>>>

    fun updateVisitedRoads(list: List<List<VisitedRoadPoint>>)

    fun areVisitedRoadsCalculated(): Boolean

    suspend fun observeTechnicalRoads(): Flow<List<PathEntity>>

    suspend fun getTechnicalRoads(): List<PathEntity>

    suspend fun observeLines(): Flow<List<PathEntity>>

}