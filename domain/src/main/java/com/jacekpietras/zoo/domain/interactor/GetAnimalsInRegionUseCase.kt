package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.repository.AnimalRepository

class GetAnimalsInRegionUseCase(
    private val animalRepository: AnimalRepository,
) {

    fun run(regionId: String): List<AnimalEntity> =
        animalRepository.getAnimals(regionId)
}