package com.jacekpietras.zoo.map.model

import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId

sealed class MapCarouselItem(
    open val name: RichText,
) {

    data class Animal(
        val id: AnimalId,
        val division: AnimalDivisionValue,
        override val name: RichText,
        val photoUrl: String?,
    ) : MapCarouselItem(name)

    data class Region(
        val id: RegionId,
        override val name: RichText,
        val photoUrlLeftTop: String?,
        val photoUrlRightTop: String?,
        val photoUrlLeftBottom: String?,
        val photoUrlRightBottom: String?,
        val divisionLeftTop: AnimalDivisionValue?,
        val divisionRightTop: AnimalDivisionValue?,
        val divisionLeftBottom: AnimalDivisionValue?,
        val divisionRightBottom: AnimalDivisionValue?,
    ) : MapCarouselItem(name)
}
