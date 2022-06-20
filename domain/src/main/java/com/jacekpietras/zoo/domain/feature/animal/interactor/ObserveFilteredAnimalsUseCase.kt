package com.jacekpietras.zoo.domain.feature.animal.interactor

import com.jacekpietras.zoo.domain.feature.animal.model.AnimalFilter
import com.jacekpietras.zoo.domain.feature.animal.repository.AnimalRepository
import com.jacekpietras.zoo.domain.model.AnimalEntity
import kotlinx.coroutines.flow.Flow

class ObserveFilteredAnimalsUseCase(
    private val animalRepository: AnimalRepository,
) {

    fun run(filter: AnimalFilter): Flow<List<AnimalEntity>> =
        animalRepository.observeAnimals(filter)
}