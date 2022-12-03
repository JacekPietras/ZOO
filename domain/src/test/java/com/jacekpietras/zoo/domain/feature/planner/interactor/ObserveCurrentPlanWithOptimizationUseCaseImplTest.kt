package com.jacekpietras.zoo.domain.feature.planner.interactor

import android.text.format.DateUtils.MINUTE_IN_MILLIS
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanId
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import com.jacekpietras.zoo.domain.feature.tsp.StageTravellingSalesmanProblemSolver
import com.jacekpietras.zoo.domain.feature.tsp.TspResult
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.domain.utils.assertFlowEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

internal class ObserveCurrentPlanWithOptimizationUseCaseImplTest {

    private val mockPlanRepository = mock<PlanRepository>()
    private val mockGpsRepository = mock<GpsRepository>()
    private val mockTspSolver = mock<StageTravellingSalesmanProblemSolver>()
    private val mockObserveCurrentPlanUseCase = mock<ObserveCurrentPlanUseCase>()

    private val useCase = newUseCase(
        planRepository = mockPlanRepository,
        gpsRepository = mockGpsRepository,
        tspSolver = mockTspSolver,
        observeCurrentPlanUseCase = mockObserveCurrentPlanUseCase,
    )

    @Test
    fun `when tsp solver is not producing data, then quick solution is returned`() = runTest {
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
        whenever(mockObserveCurrentPlanUseCase.run()).thenReturn(flowOf(initialPlan))
        whenever(mockGpsRepository.observeLatestPosition()).thenReturn(flowOf())
        whenever(mockTspSolver.findShortPathAndStages(initialStages)).thenReturn(computedSolution)

        val result = useCase.run()

        assertFlowEquals(
            result,
            TspResult(initialStages),
            computedSolution,
        )

        verify(mockPlanRepository).setPlan(any())
    }

    @Test
    fun `when time passed, then optimal solution is returned`() = runTest {
        whenever(mockObserveCurrentPlanUseCase.run()).thenReturn(flowOf(initialPlan))
        whenever(mockGpsRepository.observeLatestPosition()).thenReturn(flowOf())
//        whenever(mockTspSolver.findShortPathAndStages(any())).thenReturn(computedSolution)
        whenever(mockTspSolver.findShortPathAndStages(initialStages)).thenReturn(computedSolution)
//            .thenReturn(optimalSolution)
//        whenever(mockTspSolver.findShortPathAndStages(computedStages)).thenReturn(optimalSolution)

        val result = useCase.run()
        advanceTimeBy(2 * MINUTE_IN_MILLIS)

        assertFlowEquals(
            result,
            TspResult(initialStages),
            computedSolution,
//            optimalSolution,
        )

        verify(mockPlanRepository).setPlan(any())
    }

    private fun newUseCase(
        planRepository: PlanRepository = mock(),
        gpsRepository: GpsRepository = mock(),
        tspSolver: StageTravellingSalesmanProblemSolver = mock(),
        observeCurrentPlanUseCase: ObserveCurrentPlanUseCase = mock(),
    ) = ObserveCurrentPlanWithOptimizationUseCaseImpl(
        planRepository = planRepository,
        gpsRepository = gpsRepository,
        tspSolver = tspSolver,
        observeCurrentPlanUseCase = observeCurrentPlanUseCase,
        isDebug = { false },
    )

    private companion object {

        val stage1 = newStage("stage-1-id")
        val stage2 = newStage("stage-2-id")
        val stage3 = newStage("stage-3-id")
        val stage4 = newStage("stage-4-id")
        val initialStages = listOf(
            stage4,
            stage1,
            stage3,
            stage2,
        )
        val initialPlan = PlanEntity(planId = PlanId("plan-id"), initialStages)
        val computedStages = listOf(
            stage1,
            stage2,
            stage4,
            stage3,
        )
        val computedSolution = TspResult(
            stages = computedStages,
        )
//        val optimalStages = listOf(
//            stage1,
//            stage2,
//            stage4,
//            stage3,
//        )
//        val optimalSolution = TspResult(
//            stages = optimalStages,
//        )

        private fun newStage(regionId: String = "region-id"): Stage =
            Stage.InRegion(
                region = Region.AnimalRegion(RegionId(regionId)),
                mutable = false,
                seen = false,
            )
    }
}