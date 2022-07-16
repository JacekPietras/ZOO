package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.model.RegionId

class GetRegionCenterPointUseCase(
    private val getRegionUseCase: GetRegionUseCase,
) {

    suspend fun run(regionId: RegionId): PointD =
        getRegionUseCase.run(regionId)
            .second
            .findCenter()
}