package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.repository.AnimalRepository

class GetMyszojelenUseCase(
    private val animalRepository: AnimalRepository,
) {

    suspend operator fun invoke() =
        animalRepository.scrapTestAnimals()
}