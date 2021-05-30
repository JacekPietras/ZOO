package com.jacekpietras.zoo.catalogue.feature.list.model

data class CatalogueListItem(
    val id: String,
    val name: String,
    val regionInZoo: String,
    val img: String?,
)