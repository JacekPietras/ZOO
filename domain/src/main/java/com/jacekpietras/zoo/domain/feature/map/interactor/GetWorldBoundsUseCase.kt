package com.jacekpietras.zoo.domain.feature.map.interactor

import com.jacekpietras.geometry.RectD
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository

class GetWorldBoundsUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(): RectD =
        mapRepository.getWorldBounds()
}