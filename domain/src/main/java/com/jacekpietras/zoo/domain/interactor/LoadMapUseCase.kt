package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.repository.MapRepository

class LoadMapUseCase(
    private val mapRepository: MapRepository,
) {

    suspend fun run() {
        if (!mapRepository.isMapLoaded()) {
            mapRepository.loadMap()
        }
    }
}
