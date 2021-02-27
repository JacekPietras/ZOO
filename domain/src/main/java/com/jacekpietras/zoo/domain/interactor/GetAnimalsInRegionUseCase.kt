package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.repository.AnimalRepository

class GetAnimalsInRegionUseCase(
    private val animalRepository: AnimalRepository,
) {

    operator fun invoke(regionId: String): List<AnimalEntity> =
        animalRepository.getAnimalsInRegion(regionId)
}