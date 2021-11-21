package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.core.RectD
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.VisitedRoadPoint
import kotlinx.coroutines.flow.Flow

interface MapRepository {

    suspend fun getBuildings(): Flow<List<PolygonEntity>>

    suspend fun getAviary(): Flow<List<PolygonEntity>>

    suspend fun getRoads(): Flow<List<PathEntity>>

    suspend fun getVisitedRoads(): Flow<List<List<VisitedRoadPoint>>>

    fun updateVisitedRoads(list:List<List<VisitedRoadPoint>>)

    fun areVisitedRoadsCalculated(): Boolean

    suspend fun getTechnicalRoads(): Flow<List<PathEntity>>

    suspend fun getLines(): Flow<List<PathEntity>>

    suspend fun getCurrentRegions(): List<Pair<Region, PolygonEntity>>

    suspend fun observeWorldBounds(): Flow<RectD>

    fun getWorldBounds(): RectD
}