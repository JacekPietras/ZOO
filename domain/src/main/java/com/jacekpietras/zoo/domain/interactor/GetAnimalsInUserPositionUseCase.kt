package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.AnimalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetAnimalsInUserPositionUseCase(
    private val getRegionsInUserPositionUseCase: GetRegionsInUserPositionUseCase,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
) {

    operator fun invoke(): Flow<List<AnimalEntity>> =
        getRegionsInUserPositionUseCase().map { regions ->
            regions
                .map { region -> getAnimalsInRegionUseCase(region) }
                .flatten()
                .distinct()
        }
}