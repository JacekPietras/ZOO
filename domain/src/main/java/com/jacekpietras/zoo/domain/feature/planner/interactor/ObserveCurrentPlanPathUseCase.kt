package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.core.PointD
import kotlinx.coroutines.flow.Flow

interface ObserveCurrentPlanPathUseCase {

    fun run(): Flow<List<PointD>>
}
