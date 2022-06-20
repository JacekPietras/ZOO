package com.jacekpietras.zoo.domain.feature.animal.interactor

import com.jacekpietras.zoo.domain.feature.animal.repository.AnimalRepository
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalId

class GetAnimalUseCase(
    private val animalRepository: AnimalRepository,
) {

    fun run(animalId: AnimalId): AnimalEntity =
        animalRepository.getAnimal(animalId = animalId)
}