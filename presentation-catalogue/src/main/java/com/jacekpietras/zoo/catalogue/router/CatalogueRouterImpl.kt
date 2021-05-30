package com.jacekpietras.zoo.catalogue.router

import androidx.navigation.NavController
import com.jacekpietras.zoo.catalogue.ui.CatalogueFragmentDirections

internal class CatalogueRouterImpl(
    private val navController: NavController,
) : CatalogueRouter {

    override fun navigateToAnimal() {
        navController.navigate(CatalogueFragmentDirections.navigateToAnimal())
    }
}