package com.jacekpietras.zoo.map.model

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.RegionId

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
        val distance: Double,
        val regionId: RegionId?,
    ) : MapToolbarMode()

    data class SelectedRegionMode(
        val regionsWithAnimals: List<Pair<Region, List<AnimalEntity>>>
    ) : MapToolbarMode()
}
