package com.jacekpietras.zoo.domain.model

data class RegionId(val id: String) {

    override fun equals(other: Any?): Boolean {
        return this === other || id == (other as? RegionId)?.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return id
    }
}

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
