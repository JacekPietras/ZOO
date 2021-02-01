package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow

class GetMapDataUseCase(
    private val mapRepository: MapRepository,
) {

    suspend operator fun invoke(): Flow<List<MapItemEntity>> =
        mapRepository.getMapDataUseCase()
}