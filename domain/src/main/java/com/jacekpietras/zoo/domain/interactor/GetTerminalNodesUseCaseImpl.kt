package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.GraphAnalyzer

internal class GetTerminalNodesUseCaseImpl(
    private val initializeGraphAnalyzerIfNeededUseCase: InitializeGraphAnalyzerIfNeededUseCase,
    private val graphAnalyzer: GraphAnalyzer,
) : GetTerminalNodesUseCase {

    override suspend fun run(): List<PointD> {
        initializeGraphAnalyzerIfNeededUseCase.run()
        return graphAnalyzer.getTerminalPoints()
    }
}
