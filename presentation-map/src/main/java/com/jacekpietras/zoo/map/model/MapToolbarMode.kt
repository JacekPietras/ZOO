package com.jacekpietras.zoo.map.model

import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.Region

sealed class MapToolbarMode {

    data class MapActionMode(
        val mapAction: MapAction,
    ) : MapToolbarMode()

    data class SelectedAnimalMode(
        val animal: AnimalEntity,
    ) : MapToolbarMode()

    data class SelectedRegionMode(
        val regionsWithAnimals: List<Pair<Region, List<AnimalEntity>>>
    ) : MapToolbarMode()
}
