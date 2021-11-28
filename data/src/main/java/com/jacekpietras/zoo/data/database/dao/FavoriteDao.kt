package com.jacekpietras.zoo.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacekpietras.zoo.data.database.model.FavoriteDto

@Dao
internal interface FavoriteDao {

    @Query("SELECT isFavorite FROM favorite WHERE animalId = :animalId")
    fun isFavorite(animalId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteDto)
}