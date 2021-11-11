package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.domain.repository.MapRepository

class GetRegionCenterPointUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(regionId: RegionId): PointD =
        mapRepository.getCurrentRegions()
            .firstOrNull { (currentRegionId, _) -> regionId == currentRegionId.id }
            ?.second
            ?.findCenter()
            ?: throw IllegalArgumentException("cannot find region with id: $regionId")
}