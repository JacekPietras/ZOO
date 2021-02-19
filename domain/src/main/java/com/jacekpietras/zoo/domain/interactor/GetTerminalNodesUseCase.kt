package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetTerminalNodesUseCase(
    private val getRoadsUseCase: GetRoadsUseCase,
    private val getTechnicalRoadsUseCase: GetTechnicalRoadsUseCase,
) {

    operator fun invoke(): Flow<List<PointD>> =
        combine(
            getRoadsUseCase(),
            getTechnicalRoadsUseCase(),
        ) { roads, technical ->
            GraphAnalyzer.initialize(roads, technical)
            GraphAnalyzer.getTerminalPoints()
        }
}