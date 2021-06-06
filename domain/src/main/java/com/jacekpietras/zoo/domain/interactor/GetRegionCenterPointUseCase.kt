package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.repository.MapRepository

class GetRegionCenterPointUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(regionId: String): PointD =
        mapRepository.getCurrentRegions()
            .first { (currentRegionId, _) -> regionId == currentRegionId }
            .second
            .findCenter()
}