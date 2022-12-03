package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import com.jacekpietras.zoo.domain.feature.tsp.StageTravellingSalesmanProblemSolver
import com.jacekpietras.zoo.domain.feature.tsp.TspResult
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.domain.utils.assertFlowEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

internal class ObserveCurrentPlanWithOptimizationUseCaseImplTest {

    private val mockPlanRepository = mock<PlanRepository>()
    private val mockGpsRepository = mock<GpsRepository>()
    private val mockTspSolver = mock<StageTravellingSalesmanProblemSolver>()
    private val mockObserveCurrentPlanUseCase = mock<ObserveCurrentPlanUseCase>()

    private val useCase = ObserveCurrentPlanWithOptimizationUseCaseImpl(
        planRepository = mockPlanRepository,
        gpsRepository = mockGpsRepository,
        tspSolver = mockTspSolver,
        observeCurrentPlanUseCase = mockObserveCurrentPlanUseCase,
        isDebug = { false },
    )

    @Test
    fun `when tsp solver is not producing data, then quick solution is returned`() = runTest {
        val initialStages = listOf(newStage(), newStage(), newStage())
        val initialPlan = PlanEntity(planId = mock(), initialStages)

        whenever(mockObserveCurrentPlanUseCase.run()).thenReturn(flowOf(initialPlan))
        whenever(mockGpsRepository.observeLatestPosition()).thenReturn(flowOf())

        val result = useCase.run()

        assertFlowEquals(
            result,
            TspResult(initialStages),
        )

        verifyNoInteractions(mockPlanRepository)
    }

    @Test
    fun `when tsp solver is producing data, then better solution is returned`() = runTest {
        val stage1 = newStage("stage-1-id")
        val stage2 = newStage("stage-2-id")
        val stage3 = newStage("stage-3-id")
        val initialStages = listOf(
            stage1,
            stage3,
            stage2,
        )
        val initialPlan = PlanEntity(planId = mock(), initialStages)
        val computedStages = listOf(
            stage1,
            stage2,
            stage3,
        )
        val computedSolution = TspResult(
            stages = computedStages,
            stops = listOf(mock()),
            path = listOf(mock()),
        )

        whenever(mockObserveCurrentPlanUseCase.run()).thenReturn(flowOf(initialPlan))
        whenever(mockGpsRepository.observeLatestPosition()).thenReturn(flowOf())
        whenever(mockTspSolver.findShortPathAndStages(initialStages)).thenReturn(computedSolution)

        val result = useCase.run()
        advanceUntilIdle()

        assertFlowEquals(
            result,
            TspResult(initialStages),
            computedSolution,
        )
    }

    private fun newStage(regionId: String = "region-id"): Stage =
        Stage.InRegion(
            region = Region.AnimalRegion(RegionId(regionId)),
            mutable = false,
            seen = false,
        )
}