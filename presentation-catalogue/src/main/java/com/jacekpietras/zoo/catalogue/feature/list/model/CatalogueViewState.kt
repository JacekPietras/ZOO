package com.jacekpietras.zoo.catalogue.feature.list.model

import androidx.compose.runtime.Immutable
import com.jacekpietras.zoo.core.text.RichText

@Immutable
internal data class CatalogueViewState(
    val isToolbarVisible: Boolean = false,
    val isRegionShown: Boolean = false,
    val regionName: RichText = RichText.Empty,
    val animalList: List<CatalogueListItem> = emptyList(),
    val filter: AnimalDivision? = null,
    val filtersVisible: Boolean = true,
    val searchVisible: Boolean = false,
    val searchText: String = "",
)