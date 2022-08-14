package com.jacekpietras.zoo.domain.feature.map.interactor

import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import kotlinx.coroutines.flow.Flow

class ObserveWaterUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(): Flow<List<PolygonEntity>> =
        mapRepository.observeWater()
}
