package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.AnimalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetAnimalsInUserPositionUseCase(
    private val getRegionsInUserPositionUseCase: GetRegionsInUserPositionUseCase,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
) {

    fun run(): Flow<List<AnimalEntity>> =
        getRegionsInUserPositionUseCase.run().map { regions ->
            regions
                .map { region -> getAnimalsInRegionUseCase.run(region) }
                .flatten()
                .distinct()
        }
}