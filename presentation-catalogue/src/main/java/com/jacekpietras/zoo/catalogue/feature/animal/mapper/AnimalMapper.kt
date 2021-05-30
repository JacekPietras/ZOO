package com.jacekpietras.zoo.catalogue.feature.animal.mapper

import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalState
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState

internal class AnimalMapper {

    fun from(state: AnimalState): AnimalViewState =
        AnimalViewState(
            title = state.animal.name
        )
}