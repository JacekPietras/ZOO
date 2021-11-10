package com.jacekpietras.zoo.map.router

import android.net.Uri
import androidx.navigation.NavController
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.map.ui.ComposableMapFragmentDirections

internal class MapRouterImpl(
    private val navController: NavController,
) : MapRouter {

    override fun navigateToCamera() {
        navController.navigate(ComposableMapFragmentDirections.navigateToCamera())
    }

    override fun navigateToAnimal(animalId: AnimalId) {
        navController.navigate(Uri.parse("zoo://fragmentAnimal?animalId=${animalId.id}"))
    }

    override fun navigateToAnimalList(regionId: String) {
        navController.navigate(Uri.parse("zoo://fragmentCatalogue?regionId=$regionId"))
    }

    override fun goBack() {
        navController.navigateUp()
    }
}