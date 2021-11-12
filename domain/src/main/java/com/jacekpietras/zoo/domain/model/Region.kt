package com.jacekpietras.zoo.domain.model

data class RegionId(val id: String)

sealed class Region(
    open val id: RegionId
) {

    data class AnimalRegion(
        override val id: RegionId,
    ) : Region(id)

    data class RestaurantRegion(
        override val id: RegionId,
    ) : Region(id)

    data class WcRegion(
        override val id: RegionId,
    ) : Region(id)

    data class ExitRegion(
        override val id: RegionId,
    ) : Region(id)

}
