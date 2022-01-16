package com.jacekpietras.zoo.domain.feature.favorites.interactor

import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.feature.favorites.repository.FavoritesRepository

class SetAnimalFavoriteUseCase(
    private val favoritesRepository: FavoritesRepository,
) {

    suspend fun run(animalId: AnimalId, isFavorite: Boolean) {
        favoritesRepository.setFavorite(animalId, isFavorite)
    }
}
