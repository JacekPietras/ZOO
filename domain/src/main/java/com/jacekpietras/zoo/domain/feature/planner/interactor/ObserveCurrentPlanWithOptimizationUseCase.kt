package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import kotlinx.coroutines.flow.Flow

interface ObserveCurrentPlanWithOptimizationUseCase {

    fun run(): Flow<Pair<List<Stage>, List<PointD>>>
}
