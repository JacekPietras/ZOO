package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow

class GetAviaryUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(): Flow<List<PolygonEntity>> =
        mapRepository.getAviary()
}