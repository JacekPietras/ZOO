package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.repository.AnimalRepository

class GetMyszojelenUseCase(
    private val animalRepository: AnimalRepository,
) {

    suspend operator fun invoke(): AnimalEntity =
        animalRepository.getMyszojelen()
}