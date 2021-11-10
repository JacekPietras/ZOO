package com.jacekpietras.zoo.catalogue.feature.list.router

import androidx.navigation.NavController
import com.jacekpietras.zoo.catalogue.feature.list.ui.CatalogueFragmentDirections
import com.jacekpietras.zoo.domain.model.AnimalId

internal class CatalogueRouterImpl(
    private val navController: NavController,
) : CatalogueRouter {

    override fun navigateToAnimal(animalId: AnimalId) {
        navController.navigate(CatalogueFragmentDirections.navigateToAnimal(animalId = animalId.id))
    }

    override fun goBack() {
        navController.navigateUp()
    }
}