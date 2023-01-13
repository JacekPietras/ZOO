package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.geometry.BuildConfig
import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanId
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import com.jacekpietras.zoo.domain.feature.tsp.StageTSPSolver
import com.jacekpietras.zoo.domain.feature.tsp.model.TspResult
import com.jacekpietras.zoo.domain.model.Region
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.measureTime

internal class ObserveCurrentPlanWithOptimizationUseCaseImpl(
    private val planRepository: PlanRepository,
    private val gpsRepository: GpsRepository,
    private val tspSolver: StageTSPSolver,
    private val observeCurrentPlanUseCase: ObserveCurrentPlanUseCase,
    private val isDebug: () -> Boolean = { BuildConfig.DEBUG },
) : ObserveCurrentPlanWithOptimizationUseCase {

    override fun run(): Flow<TspResult> =
        Storage<List<Stage>>(emptyList()).let { calculation ->
            Storage<Job?>(null).let { job ->
                observeCurrentPlanUseCase.run()
                    .requireSomePlan()
                    .distinctUntilChanged { _, new ->
                        new.stages == calculation.take().filter { it !is Stage.InUserPosition } && new.stages.size > 2
                    }
                    .moveExitToEnd()
                    .combineWithUserPosition()
                    .refreshPeriodically(MINUTE, skipWhen = { job.take() != null })
                    .onEach { job.purge() }
                    .pushAndDo(
                        fast = ::emitPlanWithoutCalculations,
                        long = { plan, collector ->
                            with(CoroutineScope(Dispatchers.Default)) {
                                launch {
                                    printMeasure {
                                        val (seen, notSeen) = plan.stages.partition(::isSeen)
                                        val result = tspSolver
                                            .findShortPathAndStages(notSeen)
                                            .addSeen(seen)
                                        job.save(null)
                                        if (!isActive) return@launch
                                        collector.emit(result)
                                        saveBetterPlan(plan, result.stages)
                                    }
                                }.let(job::save)
                            }
                        },
                    )
                    .onEach { calculation.save(it.stages) }
                    .distinctUntilChanged()
            }
        }

    private inline fun printMeasure(block: () -> Unit) {
        val measure = measureTime(block)
        Timber.d("Optimization step took $measure")
    }

    private suspend fun emitPlanWithoutCalculations(
        plan: PlanEntity,
        collector: FlowCollector<TspResult>,
    ) {
        collector.emit(TspResult(plan.stages))
    }

    private fun isSeen(stage: Stage) =
        stage is Stage.InRegion && stage.seen

    private fun TspResult.addSeen(seen: List<Stage>) =
        copy(stages = seen + stages)

    private fun Storage<Job?>.purge() {
        take()?.cancel()
        save(null)
    }

    private suspend fun saveBetterPlan(
        currentPlan: PlanEntity,
        resultStages: List<Stage>
    ) {
        if (currentPlan.stages != resultStages) {
            if (isDebug()) {
                val currentDistance = currentPlan.stages.distance()
                val resultDistance = resultStages.distance()
                Timber.d("Found better tsp solution $currentDistance -> $resultDistance")
            }
            with(CoroutineScope(Dispatchers.Default)) {
                launch {
                    planRepository.setPlan(currentPlan.copy(stages = resultStages))
                }
            }
        }
    }

    private fun Flow<PlanEntity?>.requireSomePlan(): Flow<PlanEntity> =
        map { it ?: newEmptyPlan() }

    private fun <T> Flow<T>.refreshPeriodically(period: Long, skipWhen: () -> Boolean) =
        combine(tickerFlow(period, skipWhen)) { it, _ -> it }

    private fun tickerFlow(period: Long, skipWhen: () -> Boolean) = flow {
        while (currentCoroutineContext().isActive) {
            if (!skipWhen()) {
                emit(Unit)
            }
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
            if (userPosition != null) {
                val userStage = userPosition.let(Stage::InUserPosition)
                val (seen, notSeen) = plan.stages.partition(::isSeen)

                plan.copy(stages = seen + userStage + notSeen)
            } else {
                plan
            }
        }

    @Suppress("USELESS_CAST")
    private fun observeUserPosition(): Flow<PointD?> = with(Storage(0L)) {
        gpsRepository.observeLatestPosition()
            .map { PointD(it.lon, it.lat) as PointD? }
            .map { it to System.currentTimeMillis() }
            .distinctUntilChanged { _, new -> new.second < take() + GPS_MIN_INTERVAL }
            .onEach { save(it.second) }
            .map { it.first }
            .onStart { emit(gpsRepository.getLatestPosition()?.let { PointD(it.lon, it.lat) }) }
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
