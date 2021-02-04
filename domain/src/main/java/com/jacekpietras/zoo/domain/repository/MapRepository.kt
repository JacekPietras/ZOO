package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import kotlinx.coroutines.flow.Flow

interface MapRepository {

    fun getBuildingsUseCase(): Flow<List<PolygonEntity>>

    fun getRoadsUseCase(): Flow<List<PathEntity>>
}