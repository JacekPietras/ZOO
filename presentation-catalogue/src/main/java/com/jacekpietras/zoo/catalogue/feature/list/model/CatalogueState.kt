package com.jacekpietras.zoo.catalogue.feature.list.model

import com.jacekpietras.zoo.domain.model.AnimalEntity

data class CatalogueState(
    val animalList: List<AnimalEntity>
)
