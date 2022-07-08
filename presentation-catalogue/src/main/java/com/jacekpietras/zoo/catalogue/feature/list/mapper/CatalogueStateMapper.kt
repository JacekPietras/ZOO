package com.jacekpietras.zoo.catalogue.feature.list.mapper

import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueListItem
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueState
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueViewState
import com.jacekpietras.zoo.core.text.Dictionary.findReadableName
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.domain.model.AnimalEntity

internal class CatalogueStateMapper(
    private val divisionMapper: DivisionMapper,
) {

    fun from(
        state: CatalogueState,
        animalList: List<AnimalEntity>,
    ) = CatalogueViewState(
        isRegionShown = state.filter.regionId != null,
        regionName = state.filter.regionId?.id?.findReadableName() ?: RichText.Empty,
        isToolbarVisible = state.filter.regionId == null,
        animalList = animalList.map { animal ->
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
