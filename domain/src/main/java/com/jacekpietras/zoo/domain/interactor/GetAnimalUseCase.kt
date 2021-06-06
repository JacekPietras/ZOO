package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.repository.AnimalRepository

class GetAnimalUseCase(
    private val animalRepository: AnimalRepository,
) {

    fun run(animalId: AnimalId): AnimalEntity? =
        animalRepository.getAnimal(animalId = animalId)
}