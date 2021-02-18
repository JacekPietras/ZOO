package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class GetTerminalNodesUseCase(
    private val getRoadsUseCase: GetRoadsUseCase,
    private val getTechnicalRoadsUseCase: GetTechnicalRoadsUseCase,
) {

    operator fun invoke(): Flow<List<PointD>> =
        combine(
            getRoadsUseCase(),
            getTechnicalRoadsUseCase(),
        ) { a, b -> a + b }
            .map { GraphAnalyzer(it).getTerminalPoints() }
}