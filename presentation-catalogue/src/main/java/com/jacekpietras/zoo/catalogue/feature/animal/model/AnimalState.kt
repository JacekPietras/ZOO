package com.jacekpietras.zoo.catalogue.feature.animal.model

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalEntity
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import dev.shreyaspatil.mutekt.core.annotations.GenerateMutableModel

@GenerateMutableModel
interface AnimalState {
    val animalId: AnimalId
    val animal: AnimalEntity?
    val isSeen: Boolean?
    val isFavorite: Boolean?
    val animalPositions: List<PointD>
}
