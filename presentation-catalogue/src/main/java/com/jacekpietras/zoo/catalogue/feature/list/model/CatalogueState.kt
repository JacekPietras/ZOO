package com.jacekpietras.zoo.catalogue.feature.list.model

import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalFilter

data class CatalogueState(
    val animalList: List<AnimalEntity> = emptyList(),
    val filter : AnimalFilter = AnimalFilter(),
)
