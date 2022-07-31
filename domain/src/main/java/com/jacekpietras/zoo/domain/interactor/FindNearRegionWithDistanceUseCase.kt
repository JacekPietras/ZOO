package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine
import com.jacekpietras.zoo.domain.feature.pathfinder.interactor.GetShortestPathFromUserUseCase
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.utils.toLengthInMeters

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
}
