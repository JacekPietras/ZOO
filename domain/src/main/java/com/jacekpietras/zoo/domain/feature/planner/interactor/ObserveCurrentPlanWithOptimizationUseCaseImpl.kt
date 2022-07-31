package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.geometry.BuildConfig
import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanId
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import com.jacekpietras.zoo.domain.feature.tsp.StageTravellingSalesmanProblemSolver
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.utils.measureMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

internal class ObserveCurrentPlanWithOptimizationUseCaseImpl(
    private val planRepository: PlanRepository,
    private val gpsRepository: GpsRepository,
    private val tspSolver: StageTravellingSalesmanProblemSolver,
    private val observeCurrentPlanUseCase: ObserveCurrentPlanUseCase,
) : ObserveCurrentPlanWithOptimizationUseCase {

    private var lastCalculated: List<Stage> = emptyList()
    private var lastGpsPointTimestamp: Long = 0L

    override fun run(): Flow<Pair<List<Stage>, List<PointD>>> =
        observeCurrentPlanUseCase.run()
            .onStartEmitEmptyPlan()
            .distinctUntilChanged { _, new -> new.stages == lastCalculated && new.stages.size > 2 }
            .moveExitToEnd()
            .combineWithUserPosition()
            .emptyWhenOnlyUserPosition()
            .refreshPeriodically(MINUTE)
            .pushAndDo(
                fast = { plan -> plan.stages to emptyList() },
                long = { plan ->
                    measureMap({ Timber.d("Optimization took $it") }) {
                        val (seen, notSeen) = plan.stages.partition { it is Stage.InRegion && it.seen }

                        tspSolver.findShortPathAndStages(notSeen)
                            .let { (resultStages, path) -> (seen + resultStages) to path }
                            .also { (resultStages, _) ->
                                if (plan.stages != resultStages) {
                                    saveBetterPlan(plan, resultStages)
                                }
                            }
                    }
                },
            )

    private fun Flow<PlanEntity>.onStartEmitEmptyPlan(): Flow<PlanEntity> =
        onStart {
            emit(
                PlanEntity(
                    planId = PlanId("empty"),
                    stages = emptyList(),
                )
            )
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

    private fun <T> Flow<T>.refreshPeriodically(period: Long) =
        combine(tickerFlow(period)) { it, _ -> it }

    private fun tickerFlow(period: Long) = flow {
        while (true) {
            emit(Unit)
            delay(period)
        }
    }

    private fun Flow<PlanEntity>.moveExitToEnd(): Flow<PlanEntity> =
        map { plan ->
            val exitStage = plan.stages.find { it is Stage.InRegion && it.region is Region.ExitRegion }
            if (exitStage != null) {
                plan.copy(stages = plan.stages - exitStage + exitStage)
            } else {
                plan
            }
        }

    private fun Flow<PlanEntity>.combineWithUserPosition(): Flow<PlanEntity> =
        combine(observeUserPosition()) { plan, userPosition ->
            val userStage = listOfNotNull(userPosition?.let { Stage.InUserPosition(it) })
            val (seen, notSeen) = plan.stages.partition { it is Stage.InRegion && it.seen }

            plan.copy(stages = seen + userStage + notSeen)
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

    private fun PlanEntity.haveRegions() =
        this.stages.any { stage -> stage is Stage.InRegion }

    private fun Flow<PlanEntity>.emptyWhenOnlyUserPosition(): Flow<PlanEntity> =
        mapNotNull {
            if (it.haveRegions()) {
                it
            } else {
                null
            }
        }

    private suspend fun List<Stage>.distance(): Double =
        zipWithNext { a, b -> tspSolver.getDistance(a, b) }.sum()

    private fun <T, Y> Flow<Y>.pushAndDo(
        fast: (Y) -> T,
        long: suspend (Y) -> T,
    ): Flow<T> =
        object : Flow<T> {
            var mapped = false

            override suspend fun collect(collector: FlowCollector<T>) {
                this@pushAndDo.collect { value ->
                    if (!mapped) {
                        collector.emit(fast(value))
                        mapped = true
                    }
                    collector.emit(long(value))
                }
            }
        }

    companion object {

        const val MINUTE = 60 * 1000L
        const val GPS_MIN_INTERVAL = 5 * 1000L
    }
}
