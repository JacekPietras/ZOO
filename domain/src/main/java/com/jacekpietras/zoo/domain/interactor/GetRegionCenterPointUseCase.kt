package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.repository.MapRepository

class GetRegionCenterPointUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(regionId: String): PointD =
        mapRepository.getCurrentRegions()
            .firstOrNull { (currentRegionId, _) -> regionId == currentRegionId }
            ?.second
            ?.findCenter()
            ?: throw IllegalArgumentException("cannot find region with id: $regionId")
}