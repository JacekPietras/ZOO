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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

internal class ObserveCurrentPlanWithOptimizationUseCaseImpl(
    private val planRepository: PlanRepository,
    private val gpsRepository: GpsRepository,
    private val tspSolver: StageTravellingSalesmanProblemSolver,
    private val observeCurrentPlanUseCase: ObserveCurrentPlanUseCase,
) : ObserveCurrentPlanWithOptimizationUseCase {

    override fun run(): Flow<Pair<List<Stage>, List<PointD>>> =
        Storage<List<Stage>>(emptyList()).let { calculation ->
            Storage<Job?>(null).let { job ->
                observeCurrentPlanUseCase.run()
                    .onEach {
                        job.take()?.cancel()
                        job.save(null)
                    }
                    .map { it ?: newEmptyPlan() }
                    .distinctUntilChanged { _, new ->
                        new.stages == calculation.take().filter { it !is Stage.InUserPosition } && new.stages.size > 2
                    }
                    .moveExitToEnd()
                    .combineWithUserPosition()
                    .refreshPeriodically(MINUTE)
                    .pushAndDo(
                        fast = { plan, collector ->
                            @Suppress("RemoveExplicitTypeArguments")
                            collector.emit(plan.stages to emptyList<PointD>())
                        },
                        long = { plan, collector ->
                            measureMap({ Timber.d("Optimization took $it") }) {
                                val (seen, notSeen) = plan.stages.partition { it is Stage.InRegion && it.seen }
                                coroutineScope {
                                    val findingJob = launch(Dispatchers.Default) {
                                        val result = tspSolver.findShortPathAndStages(notSeen)
                                            .let { (resultStages, path) -> (seen + resultStages) to path }
                                        val (resultStages, _) = result
                                        job.save(null)
                                        collector.emit(result)
                                        if (plan.stages != resultStages) {
                                            saveBetterPlan(plan, result.first)
                                        }
                                    }
                                    job.save(findingJob)
                                }
                            }
                        },
                    )
                    .onEach { (resultStages, _) ->
                        calculation.save(resultStages)
                    }
            }
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
            val exitStage = plan.stages.find { it.isExit() }
            if (exitStage != null) {
                plan.copy(stages = plan.stages.filter { !it.isExit() } + exitStage)
            } else {
                plan
            }
        }

    private fun Stage.isExit() = this is Stage.InRegion && this.region is Region.ExitRegion

    private fun Flow<PlanEntity>.combineWithUserPosition(): Flow<PlanEntity> =
        combine(observeUserPosition()) { plan, userPosition ->
            val userStage = listOfNotNull(userPosition?.let { Stage.InUserPosition(it) })
            val (seen, notSeen) = plan.stages.partition { it is Stage.InRegion && it.seen }

            plan.copy(stages = seen + userStage + notSeen)
        }

    @Suppress("USELESS_CAST")
    private fun observeUserPosition(): Flow<PointD?> = with(Storage(0L)) {
        gpsRepository.observeLatestPosition()
            .map { PointD(it.lon, it.lat) as PointD? }
            .map { it to System.currentTimeMillis() }
            .distinctUntilChanged { _, new -> new.second < take() + GPS_MIN_INTERVAL }
            .onEach { save(it.second) }
            .map { it.first }
            .onStart { emit(null) }
            .distinctUntilChanged()
    }

    private suspend fun List<Stage>.distance(): Double =
        zipWithNext { a, b -> tspSolver.getDistance(a, b) }.sum()

    private fun <Y, T> Flow<Y>.pushAndDo(
        fast: suspend (Y, FlowCollector<T>) -> Unit,
        long: suspend (Y, FlowCollector<T>) -> Unit,
    ): Flow<T> =
        object : Flow<T> {
            var mapped = false

            override suspend fun collect(collector: FlowCollector<T>) {
                this@pushAndDo.collect { value ->
                    if (!mapped) {
                        fast(value, collector)
                        mapped = true
                    }
                    long(value, collector)
                }
            }
        }

    private fun newEmptyPlan() =
        PlanEntity(
            planId = PlanId(EMPTY_PLAN_ID),
            stages = emptyList(),
        )

    private inner class Storage<T>(private var lastCalculated: T) {

        fun take() = lastCalculated

        fun save(it: T) {
            lastCalculated = it
        }
    }

    companion object {

        const val EMPTY_PLAN_ID = "EMPTY"
        const val MINUTE = 60 * 1000L
        const val GPS_MIN_INTERVAL = 5 * 1000L
    }
}
