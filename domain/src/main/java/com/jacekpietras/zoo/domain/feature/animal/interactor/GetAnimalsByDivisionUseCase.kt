package com.jacekpietras.zoo.domain.feature.animal.interactor

import com.jacekpietras.zoo.domain.feature.animal.repository.AnimalRepository
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.Division

class GetAnimalsByDivisionUseCase(
    private val animalRepository: AnimalRepository,
) {

    fun run(division: Division? = null): List<AnimalEntity> =
        animalRepository.getAnimals(division)
}