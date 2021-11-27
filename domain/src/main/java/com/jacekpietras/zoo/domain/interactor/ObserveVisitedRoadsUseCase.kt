package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveVisitedRoadsUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(): Flow<List<PathEntity>> =
        mapRepository.observeVisitedRoads()
            .map { edges ->
                mutableListOf<PathEntity>().apply {
                    edges.forEach { edge ->
                        when (edge) {
                            is VisitedRoadEdge.Fully -> add(edge.toPath())
                            is VisitedRoadEdge.Partially -> addAll(edge.toPath())
                        }
                    }
                }
            }
}