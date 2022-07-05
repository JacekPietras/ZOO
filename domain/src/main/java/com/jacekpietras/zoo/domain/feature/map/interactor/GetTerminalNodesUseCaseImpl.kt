package com.jacekpietras.zoo.domain.feature.map.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.GraphAnalyzer
import com.jacekpietras.zoo.domain.feature.pathfinder.interactor.InitializeGraphAnalyzerIfNeededUseCase

internal class GetTerminalNodesUseCaseImpl(
    private val initializeGraphAnalyzerIfNeededUseCase: InitializeGraphAnalyzerIfNeededUseCase,
    private val graphAnalyzer: GraphAnalyzer,
) : GetTerminalNodesUseCase {

    override suspend fun run(): List<PointD> {
//        initializeGraphAnalyzerIfNeededUseCase.run()
//        return graphAnalyzer.getTerminalPoints()
        return emptyList()
    }
}
