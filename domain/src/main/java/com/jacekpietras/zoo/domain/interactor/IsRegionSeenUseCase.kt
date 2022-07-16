package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.geometry.polygonContains
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge

class IsRegionSeenUseCase(
    private val mapRepository: MapRepository,
) {

    suspend fun run(regionId: RegionId): Boolean {
        if (!mapRepository.areVisitedRoadsCalculated()) return false

        val alreadyVisited = mapRepository.getVisitedRoads()
        val region = mapRepository.getCurrentRegions().first { it.first.id == regionId }.second

        alreadyVisited.forEach { edge ->
            edge.forEachPath {
                it.vertices.forEach { point ->
                    if (polygonContains(region.vertices, point)) return true
                }
            }
        }

        return false
    }

    private inline fun VisitedRoadEdge.forEachPath(block: (MapItemEntity.PathEntity) -> Unit) {
        when (this) {
            is VisitedRoadEdge.Fully -> {
                block(toPath())
            }
            is VisitedRoadEdge.Partially -> {
                toPath().forEach {
                    block(it)
                }
            }
        }
    }
}
