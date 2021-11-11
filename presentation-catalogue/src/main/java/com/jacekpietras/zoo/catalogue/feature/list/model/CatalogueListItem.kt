package com.jacekpietras.zoo.catalogue.feature.list.model

import com.jacekpietras.zoo.domain.model.RegionId

data class CatalogueListItem(
    val id: String,
    val name: String,
    val regionInZoo: List<RegionId>,
    val img: String?,
)