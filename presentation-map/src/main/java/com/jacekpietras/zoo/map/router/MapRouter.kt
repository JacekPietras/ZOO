package com.jacekpietras.zoo.map.router

import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId

internal interface MapRouter {

    fun navigateToCamera()

    fun navigateToAnimal(animalId: AnimalId)

    fun navigateToAnimalList(regionId: RegionId)

    fun goBack()
}