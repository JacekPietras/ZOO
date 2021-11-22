package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.GraphAnalyzer

class GetShortestPathUseCase(
    private val initializeGraphAnalyzerIfNeededUseCase: InitializeGraphAnalyzerIfNeededUseCase,
) {

    suspend fun run(start: PointD, end: PointD): List<PointD> {
        initializeGraphAnalyzerIfNeededUseCase.run()
        return GraphAnalyzer.getShortestPath(
            endPoint = end,
            startPoint = start,
        )
    }
}