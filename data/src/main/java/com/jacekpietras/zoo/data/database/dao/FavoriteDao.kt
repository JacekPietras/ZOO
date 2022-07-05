package com.jacekpietras.zoo.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacekpietras.zoo.data.database.model.FavoriteDto
import kotlinx.coroutines.flow.Flow

@Dao
internal interface FavoriteDao {

    @Query("SELECT isFavorite FROM favorite WHERE animalId = :animalId")
    suspend fun isFavorite(animalId: String): Boolean?

    @Query("SELECT animalId FROM favorite WHERE isFavorite = 1")
    fun observeFavorites(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteDto)
}