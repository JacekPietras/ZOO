package com.jacekpietras.zoo.domain.feature.favorites.repository

import com.jacekpietras.zoo.domain.model.AnimalId
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {

    suspend fun isFavorite(animalId: AnimalId): Boolean

    suspend fun setFavorite(animalId: AnimalId, isFavorite: Boolean)

    fun observeFavorites(): Flow<List<AnimalId>>
}