package com.jacekpietras.zoo.catalogue.feature.animal.model

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalEntity
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId

internal data class AnimalState(
    val animalId: AnimalId,
    val animal: AnimalEntity? = null,
    val isSeen: Boolean? = null,
    val isFavorite: Boolean? = null,
    val animalPositions: List<PointD> = emptyList(),
)
