@file:OptIn(ExperimentalStdlibApi::class)

package com.jacekpietras.zoo.domain.feature.planner.interactor

import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.text.format.DateUtils.SECOND_IN_MILLIS
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanId
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import com.jacekpietras.zoo.domain.feature.tsp.StageTravellingSalesmanProblemSolver
import com.jacekpietras.zoo.domain.feature.tsp.model.TspResult
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.domain.utils.assertFlowEquals
import com.jacekpietras.zoo.domain.utils.assertFlowEqualsWithTimeout
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
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
        whenever(mockObserveCurrentPlanUseCase.run()).thenReturn(flowOf(initialPlanFor4))
        whenever(mockGpsRepository.observeLatestPosition()).thenReturn(flowOf())

        val result = useCase.run()

        assertFlowEquals(
            result,
            TspResult(initialStagesFor4),
        )

        verifyNoInteractions(mockPlanRepository)
    }

    @Test
    fun `when tsp solver is producing data, then better solution is returned`() = runTest {
        whenever(mockObserveCurrentPlanUseCase.run()).thenReturn(flowOf(initialPlanFor4))
        whenever(mockGpsRepository.observeLatestPosition()).thenReturn(flowOf())
        whenever(mockTspSolver.findShortPathAndStages(initialStagesFor4)).doReturn(computedSolutionFor4)

        val result = useCase.run()

        assertFlowEqualsWithTimeout(
            flow = result,
            TspResult(initialStagesFor4),
            computedSolutionFor4,
        )

        verify(mockPlanRepository).setPlan(any())
    }

    @Test
    fun `when time passed, then optimal solution is returned`() = runTest {
        val planFlow = flow {
            emit(initialPlanFor4)
            delay(90 * SECOND_IN_MILLIS)
            emit(initialPlanFor5)
        }

        whenever(mockObserveCurrentPlanUseCase.run()).thenReturn(planFlow)
        whenever(mockGpsRepository.observeLatestPosition()).thenReturn(flowOf())
        whenever(mockTspSolver.findShortPathAndStages(initialStagesFor4)).thenReturn(computedSolutionFor4, optimalSolutionFor4)
        whenever(mockTspSolver.findShortPathAndStages(initialStagesFor5)).thenReturn(computedSolutionFor5)

        val result = useCase.run()

        assertFlowEquals(
            flow = result,
            TspResult(initialStagesFor4),
            computedSolutionFor4,
        )

        advanceTimeBy(MINUTE_IN_MILLIS)
        assertFlowEqualsWithTimeout(
            flow = result,
            optimalSolutionFor4,
        )
        verify(mockPlanRepository, times(2)).setPlan(any())

        assertFlowEqualsWithTimeout(
            flow = result,
            timeout = 2 * MINUTE_IN_MILLIS,
            optimalSolutionFor4,
            computedSolutionFor5,
        )
        verify(mockPlanRepository, times(4)).setPlan(any())
    }

    private fun newUseCase(
        planRepository: PlanRepository = mockPlanRepository,
        gpsRepository: GpsRepository = mockGpsRepository,
        tspSolver: StageTravellingSalesmanProblemSolver = mockTspSolver,
        observeCurrentPlanUseCase: ObserveCurrentPlanUseCase = mockObserveCurrentPlanUseCase,
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
        val stage5 = newStage("stage-5-id")

        val initialStagesFor4 = listOf(
            stage4,
            stage1,
            stage3,
            stage2,
        )
        val initialPlanFor4 = PlanEntity(planId = PlanId("plan-id"), initialStagesFor4)
        val computedStagesFor4 = listOf(
            stage1,
            stage2,
            stage4,
            stage3,
        )
        val computedSolutionFor4 = TspResult(
            stages = computedStagesFor4,
        )
        val optimalStagesFor4 = listOf(
            stage1,
            stage2,
            stage3,
            stage4,
        )
        val optimalSolutionFor4 = TspResult(
            stages = optimalStagesFor4,
        )

        val initialStagesFor5 = listOf(
            stage4,
            stage1,
            stage3,
            stage5,
            stage2,
        )
        val initialPlanFor5 = PlanEntity(planId = PlanId("plan-id"), initialStagesFor5)
        val computedStagesFor5 = listOf(
            stage1,
            stage2,
            stage4,
            stage5,
            stage3,
        )
        val computedSolutionFor5 = TspResult(
            stages = computedStagesFor5,
        )

        private fun newStage(regionId: String = "region-id"): Stage =
            Stage.InRegion(
                region = Region.AnimalRegion(RegionId(regionId)),
                mutable = false,
                seen = false,
            )
    }
}