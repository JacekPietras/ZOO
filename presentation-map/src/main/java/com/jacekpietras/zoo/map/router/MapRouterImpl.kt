package com.jacekpietras.zoo.map.router

import androidx.navigation.NavController
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.map.ui.ComposableMapFragmentDirections

internal class MapRouterImpl(
    private val navController: NavController,
) : MapRouter {

    override fun navigateToAnimal(animalId: AnimalId) {
//        navController.navigate(ComposableMapFragmentDirections.navigateToAnimal(animalId = animalId.id))
    }

    override fun navigateToCamera() {
        navController.navigate(ComposableMapFragmentDirections.navigateToCamera())
    }
}