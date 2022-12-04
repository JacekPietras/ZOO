package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.tsp.model.TspResult
import kotlinx.coroutines.flow.Flow

interface ObserveCurrentPlanWithOptimizationUseCase {

    fun run(): Flow<TspResult>
}
