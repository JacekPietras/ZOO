package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalFilter
import com.jacekpietras.zoo.domain.repository.AnimalRepository
import kotlinx.coroutines.flow.Flow

class ObserveFilteredAnimalsUseCase(
    private val animalRepository: AnimalRepository,
) {

    fun run(filter: AnimalFilter): Flow<List<AnimalEntity>> =
        animalRepository.observeAnimals(filter)
}