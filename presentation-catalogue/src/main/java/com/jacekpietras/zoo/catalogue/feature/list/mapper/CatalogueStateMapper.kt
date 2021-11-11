package com.jacekpietras.zoo.catalogue.feature.list.mapper

import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueListItem
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueState
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueViewState
import com.jacekpietras.zoo.core.RegionMapper
import com.jacekpietras.zoo.core.text.Text

internal class CatalogueStateMapper(
    private val divisionMapper: DivisionMapper,
    private val regionMapper: RegionMapper,
) {

    fun from(state: CatalogueState) = CatalogueViewState(
        isRegionShown = state.filter.regionId != null,
        regionName = state.filter.regionId?.id?.let(regionMapper::from) ?: Text.Empty,
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
