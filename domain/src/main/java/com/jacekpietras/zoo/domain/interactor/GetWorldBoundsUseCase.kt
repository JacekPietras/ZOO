package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.RectD
import com.jacekpietras.zoo.domain.repository.MapRepository

class GetWorldBoundsUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(): RectD =
        mapRepository.getWorldBounds()
}