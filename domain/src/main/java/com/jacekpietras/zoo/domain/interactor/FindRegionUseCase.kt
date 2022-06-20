package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import com.jacekpietras.zoo.domain.model.Region

class FindRegionUseCase(
    private val mapRepository: MapRepository,
) {

    suspend fun run(condition: (Region) -> Boolean): List<Region> =
        mapRepository.getCurrentRegions()
            .filter { (currentRegionId, _) -> condition(currentRegionId) }
            .map(Pair<Region, MapItemEntity.PolygonEntity>::first)
}