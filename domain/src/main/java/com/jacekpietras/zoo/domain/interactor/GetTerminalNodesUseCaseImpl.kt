package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository

internal class GetTerminalNodesUseCaseImpl(
    private val initializeGraphAnalyzerIfNeededUseCase: InitializeGraphAnalyzerIfNeededUseCase,
    private val graphAnalyzer: GraphAnalyzer,
    private val planRepository: PlanRepository,
    private val getRegionCenterPointUseCase: GetRegionCenterPointUseCase,
) : GetTerminalNodesUseCase {

    override suspend fun run(): List<PointD> {
//        initializeGraphAnalyzerIfNeededUseCase.run()
//        return graphAnalyzer.getTerminalPoints()
        return planRepository.getPlan(CURRENT_PLAN_ID)?.stages?.map { getRegionCenterPointUseCase.run(it.regionId) }
            ?: emptyList()
    }
}
