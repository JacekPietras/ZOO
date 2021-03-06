package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.repository.AnimalRepository

class LoadAnimalsUseCase(
    private val animalRepository: AnimalRepository,
) {

    suspend fun run() =
        animalRepository.scrapTestAnimals()
}