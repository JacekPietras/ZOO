package com.jacekpietras.zoo.catalogue.feature.list.model

internal data class CatalogueViewState(
    val animalList: List<CatalogueListItem> = emptyList(),
    val filterList: List<AnimalDivision> = emptyList(),
    val filtersVisible: Boolean = true,
    val searchVisible: Boolean = false,
)