package com.jacekpietras.zoo.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite")
internal data class FavoriteDto(
    @PrimaryKey val animalId: String,
    val isFavorite: Boolean,
)
