package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.zoo.domain.model.MapItemEntity
import kotlinx.coroutines.flow.Flow

interface MapRepository {

   suspend fun getMapDataUseCase(): Flow<List<MapItemEntity>>
}