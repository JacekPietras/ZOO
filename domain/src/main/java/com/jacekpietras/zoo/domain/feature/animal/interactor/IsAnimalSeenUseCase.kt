package com.jacekpietras.zoo.domain.feature.animal.interactor

import com.jacekpietras.zoo.domain.interactor.IsRegionSeenUseCase
import com.jacekpietras.zoo.domain.model.AnimalId

class IsAnimalSeenUseCase(
    private val getAnimalUseCase: GetAnimalUseCase,
    private val isRegionSeenUseCase: IsRegionSeenUseCase,
) {

    suspend fun run(animalId: AnimalId): Boolean {
        val animal = getAnimalUseCase.run(animalId)
        val regions = animal.regionInZoo
        return regions.any { isRegionSeenUseCase.run(it) }
    }
}
