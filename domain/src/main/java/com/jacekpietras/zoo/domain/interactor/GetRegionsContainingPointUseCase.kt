package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.polygonContains
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import com.jacekpietras.zoo.domain.model.Region

class GetRegionsContainingPointUseCase(
    private val mapRepository: MapRepository,
) {

    suspend fun run(point: PointD): List<Region> =
        mapRepository.getCurrentRegions()
            .filter { (_, region) -> polygonContains(region.vertices, point) }
            .map { it.first }
}