package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.core.BuildConfig
import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.MySalesmanProblemSolver
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.repository.GpsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalTime
internal class ObserveCurrentPlanPathUseCaseImpl(
    private val planRepository: PlanRepository,
    private val mySalesmanProblemSolver: MySalesmanProblemSolver,
    private val gpsRepository: GpsRepository,
) : ObserveCurrentPlanPathUseCase {

    override fun run(): Flow<List<PointD>> =
        planRepository.observePlan(CURRENT_PLAN_ID)
            .distinctUntilChanged { old, new -> old.stages == new.stages }
            .onEach {
                Timber.e("found new plan")
            }
            .refreshPeriodically(MINUTE)
            .combine(observeUserPosition()) { plan, userPosition ->
                val userStage = listOf(Stage.InUserPosition(userPosition))
                plan.copy(stages = userStage + plan.stages)
            }
            .measureMap({ Timber.w("Optimization took $it") }) { plan ->
                val result = mySalesmanProblemSolver.findShortPath(plan.stages)
                val resultStages = result.map { it.first }
                val resultDistance = result.map { it.second }.flatten()

                if (BuildConfig.DEBUG) {
                    Timber.w("Optimization ${resultStages.distance()}m")
                }

                if (plan.stages != resultStages) {
                    if (BuildConfig.DEBUG) {
                        val before = plan.stages.distance()
                        val after = resultStages.distance()
                        Timber.w("Found new path $before -> $after")
                    }
                    plan.copy(stages = resultStages)
                        .also { planRepository.setPlan(it) }
                }

                resultDistance
            }

    private fun <T, Y> Flow<T>.measureMap(onMeasure: (Duration) -> Unit, block: suspend (T) -> Y): Flow<Y> = map {
        var result: Y
        val measure = measureTime { result = block(it) }
        onMeasure(measure)
        result
    }

    private fun <T> Flow<T>.refreshPeriodically(period: Long) =
        combine(tickerFlow(period)) { it, _ -> it }

    private fun tickerFlow(period: Long) = flow {
        while (true) {
            emit(Unit)
            delay(period)
        }
    }
        .onEach {
            Timber.e("found new tick")
        }

    private fun observeUserPosition(): Flow<PointD> =
        gpsRepository.observeLatestPosition()
            .map { PointD(it.lon, it.lat) }
            .distinctUntilChanged()
            .onEach {
                Timber.e("found new point $it")
            }

    private suspend fun List<Stage>.distance(): Double =
        zipWithNext { a, b -> mySalesmanProblemSolver.getDistance(a, b) }.sum()

    companion object {

        const val MINUTE = 60 * 1000L
    }
}
