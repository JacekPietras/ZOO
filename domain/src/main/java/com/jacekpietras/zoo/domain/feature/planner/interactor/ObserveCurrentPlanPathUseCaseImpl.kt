package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.core.BuildConfig
import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.MySalesmanProblemSolver
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

internal class ObserveCurrentPlanPathUseCaseImpl(
    private val planRepository: PlanRepository,
    private val mySalesmanProblemSolver: MySalesmanProblemSolver,
) : ObserveCurrentPlanPathUseCase {

    override fun run(): Flow<List<PointD>> =
        planRepository.observePlan(CURRENT_PLAN_ID)
            .refreshPeriodically(MINUTE)
            .map { plan ->
                plan.stages
                    .let { stages ->
                        val result = mySalesmanProblemSolver.findShortPath(stages)
                        val resultStages = result.map { it.first }
                        if (BuildConfig.DEBUG) {
                            Timber.w("Optimization ${resultStages.distance()}m")
                        }

                        if (stages != resultStages) {
                            if (BuildConfig.DEBUG) {
                                val before = stages.distance()
                                val after = resultStages.distance()
                                Timber.w("Found shorter path $before -> $after")
                            }
                            plan
                                .copy(
                                    optimizationTime = System.currentTimeMillis(),
                                    stages = resultStages,
                                )
                                .also {
                                    planRepository.setPlan(it)
                                }
                        }

                        result
                    }
                    .map { it.second }
                    .flatten()
            }

    private fun <T> Flow<T>.refreshPeriodically(period: Long) =
        combine(tickerFlow(period)) { it, _ -> it }

    private fun tickerFlow(period: Long) = flow {
        while (true) {
            emit(Unit)
            delay(period)
        }
    }

    private suspend fun List<Stage>.distance(): Double =
        zipWithNext { a, b -> mySalesmanProblemSolver.getDistance(a, b) }.sum()

    companion object {

        const val MINUTE = 60 * 1000L
    }
}
