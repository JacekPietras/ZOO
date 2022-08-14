package com.jacekpietras.zoo.domain.feature.map.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import kotlinx.coroutines.flow.Flow

class ObserveTreesUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(): Flow<List<PointD>> =
        mapRepository.observeTrees()
}
