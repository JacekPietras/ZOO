package com.jacekpietras.zoo.domain.feature.animal.model

import com.jacekpietras.zoo.domain.model.RegionId

data class AnimalFilter(
    val query: String? = null,
    val division: Division? = null,
    val regionId: RegionId? = null,
)