package com.jacekpietras.zoo.catalogue.feature.list.model

internal data class CatalogueViewState(
    val animalList: List<CatalogueListItem> = emptyList(),
)