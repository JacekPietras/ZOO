package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.core.polygonContains
import com.jacekpietras.zoo.domain.repository.MapRepository

class GetRegionsContainingPointUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(point: PointD): List<String> =
        mapRepository.getCurrentRegions()
            .filter { (_, region) -> polygonContains(region.vertices, point) }
            .map { it.first }
}