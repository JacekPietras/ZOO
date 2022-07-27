package com.jacekpietras.zoo.catalogue.feature.list.router

import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId

internal interface CatalogueRouter {

    fun navigateToAnimal(animalId: AnimalId)

    fun goBack()
}