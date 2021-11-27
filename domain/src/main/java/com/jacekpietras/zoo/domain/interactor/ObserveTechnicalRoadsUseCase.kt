package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow

class ObserveTechnicalRoadsUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(): Flow<List<PathEntity>> =
        mapRepository.observeTechnicalRoads()
}