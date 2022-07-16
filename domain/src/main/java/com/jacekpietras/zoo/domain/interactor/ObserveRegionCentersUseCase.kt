package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.RegionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ObserveRegionCentersUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(): Flow<List<Pair<Region, PointD>>> =
        flow {
            val result = mapRepository.getCurrentRegions()
                .map { (region, polygon) ->
                    region to polygon.findCenter()
                }
            emit(result)
        }
}
