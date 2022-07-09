package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.feature.animal.repository.AnimalRepository
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.RegionId

class GetAnimalsInRegionUseCase(
    private val animalRepository: AnimalRepository,
) {

    suspend fun run(regionId: RegionId): List<AnimalEntity> {
        animalRepository.loadAnimals()
        return animalRepository.getAnimals(regionId)
    }
}