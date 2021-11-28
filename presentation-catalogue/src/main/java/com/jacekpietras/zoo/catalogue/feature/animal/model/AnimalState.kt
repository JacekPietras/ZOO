package com.jacekpietras.zoo.catalogue.feature.animal.model

import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalId

internal data class AnimalState(
    val animalId: AnimalId,
    val animal: AnimalEntity,
    val isSeen: Boolean? = null,
    val isFavorite: Boolean = false,
)
