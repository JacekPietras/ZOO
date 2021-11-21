package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.GraphAnalyzer

class GetShortestPathUseCase {

    suspend fun run(start: PointD, end: PointD): List<PointD> =
        GraphAnalyzer.getShortestPath(
            endPoint = end,
            startPoint = start,
        )
}