package com.jacekpietras.zoo.catalogue.feature.list.router

import androidx.navigation.NavController
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId

internal class CatalogueRouterImpl(
    private val navController: NavController,
) : CatalogueRouter {

    override fun navigateToAnimal(animalId: AnimalId) {
        navController.navigate("animal/${animalId.id}")
    }

    override fun goBack() {
        navController.navigateUp()
    }
}