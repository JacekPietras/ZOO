package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.zoo.domain.model.AnimalId

interface FavoritesRepository {

    suspend fun isFavorite(animalId: AnimalId): Boolean

    suspend fun setFavorite(animalId: AnimalId, isFavorite: Boolean)
}