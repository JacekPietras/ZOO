package com.jacekpietras.zoo.domain.feature.tsp

import com.jacekpietras.zoo.domain.feature.planner.model.Stage

internal interface StageTravellingSalesmanProblemSolver {

    suspend fun findShortPathAndStages(
        stages: List<Stage>,
    ): TspResult

    suspend fun getDistance(prev: Stage, next: Stage): Double
}
