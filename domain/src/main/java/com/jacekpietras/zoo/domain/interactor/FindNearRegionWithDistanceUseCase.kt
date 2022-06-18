package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.core.haversine
import com.jacekpietras.zoo.domain.model.Region

class FindNearRegionWithDistanceUseCase(
    private val findRegionUseCase: FindRegionUseCase,
    private val getRegionCenterPointUseCase: GetRegionCenterPointUseCase,
    private val getShortestPathUseCase: GetShortestPathFromUserUseCase,
) {

    suspend fun run(condition: (Region) -> Boolean): Pair<List<PointD>, Double>? =
        findRegionUseCase.run(condition)
            .map { getRegionCenterPointUseCase.run(regionId = it.id) }
            .map { getShortestPathUseCase.run(it) }
            .map { it to it.toLengthInMeters() }
            .minByOrNull { (_, length) -> length }

    private fun List<PointD>.toLengthInMeters(): Double =
        zipWithNext().sumOf { (p1, p2) -> haversine(p1.x, p1.y, p2.x, p2.y) }
}
