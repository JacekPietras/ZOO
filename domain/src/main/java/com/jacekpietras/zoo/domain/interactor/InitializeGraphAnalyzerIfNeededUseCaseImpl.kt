package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import com.jacekpietras.zoo.domain.repository.MapRepository

internal class InitializeGraphAnalyzerIfNeededUseCaseImpl(
    private val mapRepository: MapRepository,
    private val graphAnalyzer:GraphAnalyzer,
) : InitializeGraphAnalyzerIfNeededUseCase {

    override suspend fun run() {
        if (!graphAnalyzer.isInitialized()) {
            val roads = mapRepository.getRoads()
            val technical = mapRepository.getTechnicalRoads()
            graphAnalyzer.initialize(roads, technical)
        }
    }
}
