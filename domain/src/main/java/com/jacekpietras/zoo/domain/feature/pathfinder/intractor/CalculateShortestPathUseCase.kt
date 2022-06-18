package com.jacekpietras.zoo.domain.feature.pathfinder.intractor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.feature.planner.model.Stage

interface CalculateShortestPathUseCase {

    suspend fun run(stages: List<Stage>): List<Pair<Stage, List<PointD>>>
}