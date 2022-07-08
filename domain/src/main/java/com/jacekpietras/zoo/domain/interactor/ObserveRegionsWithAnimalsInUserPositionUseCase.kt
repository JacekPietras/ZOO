package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.Region
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class ObserveRegionsWithAnimalsInUserPositionUseCase(
    private val observeRegionsInUserPositionUseCase: ObserveRegionsInUserPositionUseCase,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
) {

    fun run(): Flow<List<Pair<Region, List<AnimalEntity>>>> =
        observeRegionsInUserPositionUseCase.run()
            .map { regions ->
                regions
                    .map { region -> region to getAnimalsInRegionUseCase.run(region.id) }
                    .filter { (_, animals) -> animals.isNotEmpty() }
            }
            .onStart { emit(emptyList()) }
}