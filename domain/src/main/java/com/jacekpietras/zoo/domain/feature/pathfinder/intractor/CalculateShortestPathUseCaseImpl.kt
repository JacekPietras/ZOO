package com.jacekpietras.zoo.domain.feature.pathfinder.intractor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.MySalesmanProblemSolver
import com.jacekpietras.zoo.domain.model.RegionId

internal class CalculateShortestPathUseCaseImpl(
    private val mySalesmanProblemSolver: MySalesmanProblemSolver,
) : CalculateShortestPathUseCase {

    override suspend fun run(regions: List<RegionId>): List<Pair<RegionId, List<PointD>>> =
        mySalesmanProblemSolver.findShortPath(regions)
}
