package com.jacekpietras.zoo.data.repository

import com.jacekpietras.zoo.data.database.dao.FavoriteDao
import com.jacekpietras.zoo.data.database.model.FavoriteDto
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.feature.favorites.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class FavoritesRepositoryImpl(
    private val favoritesDao: FavoriteDao,
) : FavoritesRepository {

    override suspend fun isFavorite(animalId: AnimalId): Boolean =
        favoritesDao.isFavorite(animalId.id)

    override suspend fun setFavorite(animalId: AnimalId, isFavorite: Boolean) {
        favoritesDao.insert(FavoriteDto(animalId.id, isFavorite))
    }

    override fun observeFavorites(): Flow<List<AnimalId>> =
        favoritesDao.observeFavorites()
            .map { it.map(::AnimalId) }
}