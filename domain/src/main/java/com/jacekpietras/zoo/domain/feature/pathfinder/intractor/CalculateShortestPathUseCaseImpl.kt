package com.jacekpietras.zoo.domain.feature.pathfinder.intractor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.SalesmanProblemSolver
import com.jacekpietras.zoo.domain.model.RegionId

internal class CalculateShortestPathUseCaseImpl(
    private val salesmanProblemSolver: SalesmanProblemSolver,
) : CalculateShortestPathUseCase {

    override suspend fun run(regions: List<RegionId>): List<Pair<RegionId, List<PointD>>> =
        salesmanProblemSolver.findShortPath(regions)
}
