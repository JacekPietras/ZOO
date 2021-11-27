package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import com.jacekpietras.zoo.domain.repository.MapRepository

class InitializeGraphAnalyzerIfNeededUseCase(
    private val mapRepository: MapRepository,
) {

    suspend fun run() {
        if (!GraphAnalyzer.isInitialized()) {
            val roads = mapRepository.getRoads()
            val technical = mapRepository.getTechnicalRoads()
            GraphAnalyzer.initialize(roads, technical)
        }
    }
}
