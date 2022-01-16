package com.jacekpietras.zoo.domain.feature.favorites.interactor

import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.feature.favorites.repository.FavoritesRepository

class IsAnimalFavoriteUseCase(
    private val favoritesRepository: FavoritesRepository,
) {

    suspend fun run(animalId: AnimalId): Boolean =
        favoritesRepository.isFavorite(animalId)
}
