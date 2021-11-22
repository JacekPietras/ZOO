package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow

class ObserveAviaryUseCase(
    private val mapRepository: MapRepository,
) {

    suspend fun run(): Flow<List<PolygonEntity>> =
        mapRepository.observeAviary()
}