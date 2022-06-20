package com.jacekpietras.zoo.domain.feature.map.interactor

import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import kotlinx.coroutines.flow.Flow

class ObserveBuildingsUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(): Flow<List<PolygonEntity>> =
        mapRepository.observeBuildings()
}