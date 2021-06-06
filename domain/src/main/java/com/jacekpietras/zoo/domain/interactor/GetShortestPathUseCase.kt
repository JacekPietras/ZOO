package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import kotlinx.coroutines.flow.firstOrNull

class GetShortestPathUseCase(
    private val getUserPositionUseCase: GetUserPositionUseCase,
) {

    suspend fun run(point: PointD): List<PointD> =
        GraphAnalyzer.getShortestPath(point, getUserPositionUseCase.run().firstOrNull())
}