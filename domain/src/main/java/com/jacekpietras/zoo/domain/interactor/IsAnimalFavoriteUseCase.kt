package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.AnimalId

class IsAnimalFavoriteUseCase(
) {

    suspend fun run(animalId: AnimalId): Boolean {
        return false
    }
}
