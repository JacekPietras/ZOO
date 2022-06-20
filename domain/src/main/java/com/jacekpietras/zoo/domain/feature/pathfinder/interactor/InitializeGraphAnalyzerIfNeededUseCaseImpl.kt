package com.jacekpietras.zoo.domain.feature.pathfinder.interactor

import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import com.jacekpietras.zoo.domain.feature.pathfinder.GraphAnalyzer

internal class InitializeGraphAnalyzerIfNeededUseCaseImpl(
    private val mapRepository: MapRepository,
    private val graphAnalyzer: GraphAnalyzer,
) : InitializeGraphAnalyzerIfNeededUseCase {

    override suspend fun run() {
        if (!graphAnalyzer.isInitialized()) {
            val roads = mapRepository.getRoads()
            val technical = mapRepository.getTechnicalRoads()
            graphAnalyzer.initialize(roads, technical)
        }
    }
}
