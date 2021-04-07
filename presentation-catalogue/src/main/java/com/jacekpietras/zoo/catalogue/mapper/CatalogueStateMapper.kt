package com.jacekpietras.zoo.catalogue.mapper

import com.jacekpietras.zoo.catalogue.model.CatalogueListItem
import com.jacekpietras.zoo.catalogue.model.CatalogueState
import com.jacekpietras.zoo.catalogue.model.CatalogueViewState

internal class CatalogueStateMapper {

    fun from(state: CatalogueState) = CatalogueViewState(
        animalList = state.animalList.map { animal ->
            CatalogueListItem(
                name = animal.name,
                regionInZoo = animal.regionInZoo,
                img = animal.photos.firstOrNull(),
            )
        }
    )
}
