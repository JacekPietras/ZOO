package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow

class GetLinesUseCase(
    private val mapRepository: MapRepository,
) {

    operator fun invoke(): Flow<List<PathEntity>> =
        mapRepository.getLines()
}