package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.PointD
import com.jacekpietras.zoo.domain.repository.GpsRepository
import com.jacekpietras.zoo.domain.repository.MapRepository
import com.jacekpietras.zoo.domain.utils.contains
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetRegionsInUserPositionUseCase(
    private val mapRepository: MapRepository,
    private val gpsRepository: GpsRepository,
) {

    operator fun invoke(): Flow<List<String>> =
        gpsRepository.observeLatestPosition().map { position ->
            val point = PointD(position.lon, position.lat)
            mapRepository.getCurrentRegions()
                .filter { region -> contains(region.second.vertices, point) }
                .map { it.first }
        }
}