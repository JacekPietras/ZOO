package com.jacekpietras.zoo.catalogue.feature.list.model

import com.jacekpietras.zoo.core.text.Text

internal data class CatalogueViewState(
    val isToolbarVisible: Boolean = false,
    val isRegionShown: Boolean = false,
    val regionName: Text = Text.Empty,
    val animalList: List<CatalogueListItem> = emptyList(),
    val filterList: List<AnimalDivision> = emptyList(),
    val filtersVisible: Boolean = true,
    val searchVisible: Boolean = false,
    val searchText: String = "",
)