package com.jacekpietras.zoo.domain.model

data class AnimalFilter(
    val query: String? = null,
    val divisions: List<Division> = emptyList(),
    val regionId: RegionId? = null,
)