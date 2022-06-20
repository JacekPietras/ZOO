package com.jacekpietras.zoo.domain.feature.map.interactor

import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository

class LoadMapUseCase(
    private val mapRepository: MapRepository,
) {

    suspend fun run() {
        if (!mapRepository.isMapLoaded()) {
            mapRepository.loadMap()
        }
    }
}
