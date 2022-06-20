package com.jacekpietras.zoo.domain.feature.map.interactor

import com.jacekpietras.core.RectD
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import kotlinx.coroutines.flow.Flow

class ObserveWorldBoundsUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(): Flow<RectD> =
        mapRepository.observeWorldBounds()
}