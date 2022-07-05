package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import com.jacekpietras.zoo.domain.model.RegionId

class GetRegionCenterPointUseCase(
    private val mapRepository: MapRepository,
) {

    suspend fun run(regionId: RegionId): PointD =
        mapRepository.getCurrentRegions()
            .firstOrNull { (currentRegionId, _) -> regionId == currentRegionId.id }
            ?.second
            ?.findCenter()
            ?: throw IllegalArgumentException("cannot find region with id: $regionId")
}