package com.jacekpietras.zoo.domain.feature.favorites.interactor

import com.jacekpietras.zoo.domain.feature.favorites.repository.FavoritesRepository
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId

class SetAnimalFavoriteUseCase(
    private val favoritesRepository: FavoritesRepository,
) {

    suspend fun run(animalId: AnimalId, isFavorite: Boolean) {
        favoritesRepository.setFavorite(animalId, isFavorite)
    }
}
