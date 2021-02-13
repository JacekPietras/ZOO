package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.core.RectD
import kotlinx.coroutines.flow.Flow

interface MapRepository {

    fun getBuildings(): Flow<List<PolygonEntity>>

    fun getRoads(): Flow<List<PathEntity>>

    fun getCurrentRegions(): List<Pair<String, PolygonEntity>>

    fun getWorldBounds(): Flow<RectD>
}