package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.GraphAnalyzer

class GetTerminalNodesUseCase(
    private val initializeGraphAnalyzerIfNeededUseCase: InitializeGraphAnalyzerIfNeededUseCase,
) {

    suspend fun run(): List<PointD> {
        initializeGraphAnalyzerIfNeededUseCase.run()
        return GraphAnalyzer.getTerminalPoints()
    }
}