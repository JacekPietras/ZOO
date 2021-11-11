package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetRegionsWithAnimalsInUserPositionUseCase(
    private val getRegionsContainingPointUseCase: GetRegionsContainingPointUseCase,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
    private val gpsRepository: GpsRepository,
) {

    fun run(): Flow<List<Pair<Region, List<AnimalEntity>>>> =
        gpsRepository.observeLatestPosition()
            .map { position -> getRegionsContainingPointUseCase.run(PointD(position.lon, position.lat)) }
            .map { regions ->
                regions
                    .map { region -> region to getAnimalsInRegionUseCase.run(region.id) }
                    .filter { (_, animals) -> animals.isNotEmpty() }
            }
}