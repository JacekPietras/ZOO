package com.jacekpietras.zoo.domain.feature.pathfinder.intractor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.MySalesmanProblemSolver
import com.jacekpietras.zoo.domain.feature.planner.model.Stage

internal class CalculateShortestPathUseCaseImpl(
    private val mySalesmanProblemSolver: MySalesmanProblemSolver,
) : CalculateShortestPathUseCase {

    override suspend fun run(stages: List<Stage>): List<Pair<Stage, List<PointD>>> =
        mySalesmanProblemSolver.findShortPath(stages)
}
