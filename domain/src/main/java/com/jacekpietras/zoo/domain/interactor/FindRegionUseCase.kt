package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.repository.MapRepository

class FindRegionUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(condition: (String) -> Boolean): List<String> =
        mapRepository.getCurrentRegions()
            .filter { (currentRegionId, _) -> condition(currentRegionId) }
            .map(Pair<String, MapItemEntity.PolygonEntity>::first)
}