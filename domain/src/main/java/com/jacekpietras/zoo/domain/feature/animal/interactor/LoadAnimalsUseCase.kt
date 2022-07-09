package com.jacekpietras.zoo.domain.feature.animal.interactor

import com.jacekpietras.zoo.domain.feature.animal.repository.AnimalRepository

class LoadAnimalsUseCase(
    private val animalRepository: AnimalRepository,
) {

    suspend fun run() {
        animalRepository.loadAnimals()
    }
}
