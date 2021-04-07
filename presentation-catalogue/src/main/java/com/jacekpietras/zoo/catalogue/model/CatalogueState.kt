package com.jacekpietras.zoo.catalogue.model

import com.jacekpietras.zoo.domain.model.AnimalEntity

data class CatalogueState(
    val animalList: List<AnimalEntity>
)
