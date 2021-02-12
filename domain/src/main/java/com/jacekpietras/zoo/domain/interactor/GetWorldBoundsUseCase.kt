package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.RectD
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow

class GetWorldBoundsUseCase(
    private val mapRepository: MapRepository,
) {

    operator fun invoke(): Flow<RectD> =
        mapRepository.getWorldBounds()
}