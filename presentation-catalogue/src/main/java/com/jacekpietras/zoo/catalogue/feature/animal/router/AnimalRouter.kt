package com.jacekpietras.zoo.catalogue.feature.animal.router

import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId

interface AnimalRouter {

    fun navigateToMap(animalId: AnimalId, regionId: RegionId? = null)

    fun navigateToWeb(link: String)

    fun navigateToWiki(link: String)
}
