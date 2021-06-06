package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetTerminalNodesUseCase(
    private val getRoadsUseCase: GetRoadsUseCase,
    private val getTechnicalRoadsUseCase: GetTechnicalRoadsUseCase,
) {

    fun run(): Flow<List<PointD>> =
        combine(
            getRoadsUseCase.run(),
            getTechnicalRoadsUseCase.run(),
        ) { roads, technical ->
            GraphAnalyzer.initialize(roads, technical)
            GraphAnalyzer.getTerminalPoints()
        }
}