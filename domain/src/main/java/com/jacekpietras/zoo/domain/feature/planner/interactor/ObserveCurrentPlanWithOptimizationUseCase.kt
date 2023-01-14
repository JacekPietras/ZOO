package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.vrp.model.VrpResult
import kotlinx.coroutines.flow.Flow

interface ObserveCurrentPlanWithOptimizationUseCase {

    fun run(): Flow<VrpResult>
}
