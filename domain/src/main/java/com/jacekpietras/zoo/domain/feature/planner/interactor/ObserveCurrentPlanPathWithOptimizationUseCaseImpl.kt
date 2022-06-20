package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.core.BuildConfig
import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import com.jacekpietras.zoo.domain.feature.tsp.MySalesmanProblemSolver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalTime
internal class ObserveCurrentPlanPathWithOptimizationUseCaseImpl(
    private val planRepository: PlanRepository,
    private val mySalesmanProblemSolver: MySalesmanProblemSolver,
    private val gpsRepository: GpsRepository,
) : ObserveCurrentPlanPathWithOptimizationUseCase {

    private var lastCalculated: List<Stage> = emptyList()
    private var lastGpsPointTimestamp: Long = 0L

    override fun run(): Flow<List<PointD>> =
        planRepository.observePlan(CURRENT_PLAN_ID)
            .distinctUntilChanged { _, new -> new.stages == lastCalculated }
            .combine(observeUserPosition()) { plan, userPosition ->
                val userStage = listOfNotNull(userPosition?.let { Stage.InUserPosition(it) })
                plan.copy(stages = userStage + plan.stages)
            }
            .refreshPeriodically(MINUTE)
            .measureMap({ Timber.d("Optimization took $it") }) { currentPlan ->
                val (resultStages, resultPath) =
                    mySalesmanProblemSolver.findShortPath(
                        stages = currentPlan.stages,
                        immutablePositions = notRegionIndexes(currentPlan)
                    )

                if (currentPlan.stages != resultStages) {
                    saveBetterPlan(currentPlan, resultStages)
                }

                resultPath
            }

    private suspend fun saveBetterPlan(
        currentPlan: PlanEntity,
        resultStages: List<Stage>
    ) {
        if (BuildConfig.DEBUG) {
            val currentDistance = currentPlan.stages.distance()
            val resultDistance = resultStages.distance()
            Timber.d("Found better tsp solution $currentDistance -> $resultDistance")
        }
        lastCalculated = resultStages.filter { it !is Stage.InUserPosition }
        planRepository.setPlan(currentPlan.copy(stages = resultStages))
    }

    private fun notRegionIndexes(plan: PlanEntity) =
        plan.stages
            .mapIndexed { i, stage ->
                if (stage is Stage.InRegion) {
                    null
                } else {
                    i
                }
            }
            .filterNotNull()

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

    @Suppress("USELESS_CAST")
    private fun observeUserPosition(): Flow<PointD?> =
        gpsRepository.observeLatestPosition()
            .map { PointD(it.lon, it.lat) as PointD? }
            .map { it to System.currentTimeMillis() }
            .distinctUntilChanged { _, new -> new.second < lastGpsPointTimestamp + GPS_MIN_INTERVAL }
            .onEach { lastGpsPointTimestamp = it.second }
            .map { it.first }
            .onStart { emit(null) }
            .distinctUntilChanged()

    private suspend fun List<Stage>.distance(): Double =
        zipWithNext { a, b -> mySalesmanProblemSolver.getDistance(a, b) }.sum()

    companion object {

        const val MINUTE = 60 * 1000L
        const val GPS_MIN_INTERVAL = 5 * 1000L
    }
}
