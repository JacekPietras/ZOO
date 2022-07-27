package com.jacekpietras.zoo.domain.feature.favorites.interactor

import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import com.jacekpietras.zoo.domain.feature.favorites.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class ObserveAnimalFavoritesUseCase(
    private val favoritesRepository: FavoritesRepository,
) {

    fun run(): Flow<List<AnimalId>> =
        favoritesRepository.observeFavorites()
}
