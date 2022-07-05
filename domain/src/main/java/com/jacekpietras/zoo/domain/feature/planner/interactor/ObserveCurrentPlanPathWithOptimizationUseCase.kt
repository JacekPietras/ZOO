package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.geometry.PointD
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveCurrentPlanPathWithOptimizationUseCase(
    private val observeCurrentPlanWithOptimizationUseCase: ObserveCurrentPlanWithOptimizationUseCase,
) {

    fun run(): Flow<List<PointD>> =
        observeCurrentPlanWithOptimizationUseCase.run()
            .map { (_, path) -> path }
}
