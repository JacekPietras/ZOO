package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.core.RectD
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.model.Region
import kotlinx.coroutines.flow.Flow

interface MapRepository {

    fun getBuildings(): Flow<List<PolygonEntity>>

    fun getAviary(): Flow<List<PolygonEntity>>

    fun getRoads(): Flow<List<PathEntity>>

    fun getTechnicalRoads(): Flow<List<PathEntity>>

    fun getLines(): Flow<List<PathEntity>>

    fun getCurrentRegions(): List<Pair<Region, PolygonEntity>>

    fun observeWorldBounds(): Flow<RectD>

    fun getWorldBounds(): RectD
}