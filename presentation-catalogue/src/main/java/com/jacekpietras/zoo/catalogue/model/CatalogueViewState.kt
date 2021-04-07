package com.jacekpietras.zoo.catalogue.model

internal data class CatalogueViewState(
    val animalList: List<CatalogueListItem> = emptyList(),
)