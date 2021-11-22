package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import kotlinx.coroutines.flow.firstOrNull

class GetShortestPathFromUserUseCase(
    private val getUserPositionUseCase: GetUserPositionUseCase,
    private val initializeGraphAnalyzerIfNeededUseCase: InitializeGraphAnalyzerIfNeededUseCase,
) {

    suspend fun run(point: PointD): List<PointD> {
        initializeGraphAnalyzerIfNeededUseCase.run()
        return GraphAnalyzer.getShortestPath(
            startPoint = getUserPositionUseCase.run().firstOrNull(),
            endPoint = point,
        )
    }
}