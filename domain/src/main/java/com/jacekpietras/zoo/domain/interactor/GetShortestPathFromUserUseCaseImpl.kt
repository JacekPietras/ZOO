package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import kotlinx.coroutines.flow.firstOrNull

internal class GetShortestPathFromUserUseCaseImpl(
    private val getUserPositionUseCase: GetUserPositionUseCase,
    private val initializeGraphAnalyzerIfNeededUseCase: InitializeGraphAnalyzerIfNeededUseCase,
    private val graphAnalyzer: GraphAnalyzer,
) : GetShortestPathFromUserUseCase {

    override suspend fun run(point: PointD): List<PointD> {
        initializeGraphAnalyzerIfNeededUseCase.run()
        return graphAnalyzer.getShortestPath(
            startPoint = getUserPositionUseCase.run().firstOrNull(),
            endPoint = point,
        )
    }
}
