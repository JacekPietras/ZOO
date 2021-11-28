package com.jacekpietras.zoo.catalogue.feature.animal.model

import com.jacekpietras.core.RectD
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.MapItemEntity

internal data class AnimalState(
    val animalId: AnimalId,
    val animal: AnimalEntity,
    val isSeen: Boolean? = null,
    val isFavorite: Boolean? = null,
    val worldBounds: RectD = RectD(),

    val buildings: List<MapItemEntity.PolygonEntity> = emptyList(),
    val aviary: List<MapItemEntity.PolygonEntity> = emptyList(),
    val roads: List<MapItemEntity.PathEntity> = emptyList(),
)
