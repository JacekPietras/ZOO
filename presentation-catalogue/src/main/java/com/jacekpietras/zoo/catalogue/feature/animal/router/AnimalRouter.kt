package com.jacekpietras.zoo.catalogue.feature.animal.router

import com.jacekpietras.zoo.domain.model.AnimalId

interface AnimalRouter {

    fun navigateToMap(animalId: AnimalId, regionId: String)

    fun navigateToWeb(link: String)

    fun navigateToWiki(link: String)
}
