package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.Division
import com.jacekpietras.zoo.domain.repository.AnimalRepository

class GetAnimalsByDivisionUseCase(
    private val animalRepository: AnimalRepository,
) {

    fun run(division: Division? = null): List<AnimalEntity> =
        animalRepository.getAnimals(division)
}