package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.RegionId

class GetRegionUseCase(
    private val mapRepository: MapRepository,
) {

    suspend fun run(regionId: RegionId): Pair<Region, MapItemEntity.PolygonEntity> =
        mapRepository.getCurrentRegions()
            .firstOrNull { (currentRegionId, _) -> regionId == currentRegionId.id }
            ?: throw IllegalArgumentException("cannot find region with id: $regionId")
}