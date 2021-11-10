package com.jacekpietras.zoo.catalogue.feature.list.mapper

import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueListItem
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueState
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueViewState
import com.jacekpietras.zoo.core.text.Text

internal class CatalogueStateMapper(
    private val divisionMapper: DivisionMapper
) {

    fun from(state: CatalogueState) = CatalogueViewState(
        isRegionShown = state.filter.regionId != null,
        regionName = state.filter.regionId?.let(Text::Value) ?: Text.Empty,
        isToolbarVisible = state.filter.regionId == null,
        animalList = state.animalList.map { animal ->
            CatalogueListItem(
                id = animal.id.id,
                name = animal.name,
                regionInZoo = animal.regionInZoo,
                img = animal.photos.firstOrNull(),
            )
        },
        filterList = state.filter.divisions.map(divisionMapper::from),
        filtersVisible = !state.searchOpened,
        searchVisible = state.searchOpened,
        searchText = state.filter.query ?: "",
    )
}
