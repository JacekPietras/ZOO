package com.jacekpietras.zoo.domain.repository

import android.graphics.RectF
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.model.RectD
import kotlinx.coroutines.flow.Flow

interface MapRepository {

    fun getBuildings(): Flow<List<PolygonEntity>>

    fun getRoads(): Flow<List<PathEntity>>

    fun getWorldSpace(): Flow<RectD>
}