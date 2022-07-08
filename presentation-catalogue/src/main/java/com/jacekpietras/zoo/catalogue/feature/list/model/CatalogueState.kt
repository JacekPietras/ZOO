package com.jacekpietras.zoo.catalogue.feature.list.model

import com.jacekpietras.zoo.domain.feature.animal.model.AnimalFilter

data class CatalogueState(
    val filter: AnimalFilter = AnimalFilter(),
    val searchOpened: Boolean = false,
)
