package com.jacekpietras.zoo.app.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jacekpietras.zoo.R
import com.jacekpietras.zoo.app.ui.Screen.Companion.animalIdArg
import com.jacekpietras.zoo.app.ui.Screen.Companion.regionIdArg
import com.jacekpietras.zoo.catalogue.feature.animal.ui.AnimalScreen
import com.jacekpietras.zoo.catalogue.feature.list.ui.CatalogueScreen
import com.jacekpietras.zoo.map.ui.MapScreen
import com.jacekpietras.zoo.planner.ui.PlannerScreen

@Composable
fun MainNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Map.route) {
        composable(Screen.Catalogue.route) {
            CatalogueScreen(navController, it.regionId)
        }
        composable("${Screen.Catalogue.route}/{$regionIdArg}", arguments = regionIdArg.stringType) {
            CatalogueScreen(navController, it.regionId)
        }

        composable(Screen.Map.route) {
            MapScreen(navController)
        }
        composable("${Screen.Map.route}/{$animalIdArg}/{$regionIdArg}", arguments = animalIdArg.stringType + regionIdArg.stringType) {
            MapScreen(navController, it.animalId, it.regionId)
        }

        composable(Screen.Planner.route) {
            PlannerScreen(navController)
        }

        composable("${Screen.Animal.route}/{$animalIdArg}", arguments = animalIdArg.stringType) {
            AnimalScreen(navController, it.animalId)
        }
    }
}

private val String.stringType
    get() = navArgument(this) {
        nullable = true
        type = NavType.StringType
    }.let(::listOf)

private val NavBackStackEntry.regionId get() = arguments?.getString(regionIdArg)
private val NavBackStackEntry.animalId get() = arguments?.getString(animalIdArg)

sealed class Screen(val route: String, @StringRes val title: Int, @DrawableRes val iconRes:Int) {

    object Catalogue : Screen("catalogue", R.string.title_catalogue, R.drawable.ic_elephant_24)

    object Map : Screen("map", R.string.title_map, R.drawable.ic_map_24)

    object Planner : Screen("planner", R.string.title_planner, R.drawable.ic_signs_24)

    object Animal : Screen("animal", R.string.title_animal, R.drawable.ic_elephant_24)

    companion object {

        const val regionIdArg = "regionId"
        const val animalIdArg = "animalId"
    }
}
