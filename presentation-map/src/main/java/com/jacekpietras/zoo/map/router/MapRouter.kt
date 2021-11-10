package com.jacekpietras.zoo.map.router

import com.jacekpietras.zoo.domain.model.AnimalId

internal interface MapRouter {

    fun navigateToCamera()

    fun navigateToAnimal(animalId: AnimalId)

    fun navigateToAnimalList(regionId: String)
}