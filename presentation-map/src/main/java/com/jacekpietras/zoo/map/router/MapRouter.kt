package com.jacekpietras.zoo.map.router

import com.jacekpietras.zoo.domain.model.AnimalId

internal interface MapRouter {

    fun navigateToAnimal(animalId: AnimalId)

    fun navigateToCamera()
}