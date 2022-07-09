package com.jacekpietras.zoo.domain.feature.pathfinder.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.GraphAnalyzer
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveUserPositionUseCase
import kotlinx.coroutines.flow.firstOrNull

internal class GetShortestPathFromUserUseCaseImpl(
    private val observeUserPositionUseCase: ObserveUserPositionUseCase,
    private val initializeGraphAnalyzerIfNeededUseCase: InitializeGraphAnalyzerIfNeededUseCase,
    private val graphAnalyzer: GraphAnalyzer,
) : GetShortestPathFromUserUseCase {

    override suspend fun run(point: PointD): List<PointD> {
        initializeGraphAnalyzerIfNeededUseCase.run()
        return graphAnalyzer.getShortestPath(
            startPoint = observeUserPositionUseCase.run().firstOrNull(),
            endPoint = point,
        )
    }
}
