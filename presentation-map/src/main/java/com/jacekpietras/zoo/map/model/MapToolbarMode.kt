package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.Region

sealed class MapToolbarMode {

    sealed class MapActionMode(
       open val mapAction: MapAction,
    ) : MapToolbarMode()

    data class NavigableMapActionMode(
        override val mapAction: MapAction,
        val path: List<PointD>? = null,
        val distance: Double? = null,
    ) : MapActionMode(mapAction)

    data class AroundYouMapActionMode(
        override val mapAction: MapAction,
    ) : MapActionMode(mapAction)

    data class SelectedAnimalMode(
        val animal: AnimalEntity,
    ) : MapToolbarMode()

    data class SelectedRegionMode(
        val regionsWithAnimals: List<Pair<Region, List<AnimalEntity>>>
    ) : MapToolbarMode()
}
