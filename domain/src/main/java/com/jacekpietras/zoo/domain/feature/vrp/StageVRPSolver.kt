package com.jacekpietras.zoo.domain.feature.vrp

import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.vrp.model.VrpResult

internal interface StageVRPSolver {

    suspend fun findShortPathAndStages(
        stages: List<Stage>,
    ): VrpResult

    suspend fun getDistance(prev: Stage, next: Stage): Double
}
