package com.jacekpietras.zoo.domain.feature.map.interactor

import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import kotlinx.coroutines.flow.Flow

class ObserveTechnicalRoadsUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(): Flow<List<PathEntity>> =
        mapRepository.observeTechnicalRoads()
}