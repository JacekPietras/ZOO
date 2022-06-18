package com.jacekpietras.zoo.domain.feature.pathfinder.intractor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.RegionId

interface CalculateShortestPathUseCase {

    suspend fun run(regions: List<RegionId>): List<Pair<RegionId, List<PointD>>>
}