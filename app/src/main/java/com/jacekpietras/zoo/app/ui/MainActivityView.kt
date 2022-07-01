package com.jacekpietras.zoo.app.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jacekpietras.zoo.app.ui.Screen.Companion.animalIdArg
import com.jacekpietras.zoo.app.ui.Screen.Companion.regionIdArg
import com.jacekpietras.zoo.catalogue.feature.animal.ui.AnimalScreen
import com.jacekpietras.zoo.catalogue.feature.list.ui.CatalogueScreen
import com.jacekpietras.zoo.planner.ui.PlannerScreen

@Composable
fun MainActivityView() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    BottomNavigationItemView(screen.route, screen.resource, currentDestination, navController)
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Map.route, Modifier.padding(innerPadding)) {
            composable(Screen.Catalogue.route, arguments = regionIdArg.stringType) { CatalogueScreen(navController, it.regionId) }
            composable(Screen.Map.route) { MapScreen(navController) }
            composable(Screen.Planner.route) { PlannerScreen(navController) }
            composable(Screen.Animal.route, arguments = animalIdArg.stringType) { AnimalScreen(navController, it.animalId) }
        }
    }
}

@Composable
private fun RowScope.BottomNavigationItemView(
    route: String,
    name: String,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    BottomNavigationItem(
        icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
        label = { Text(name) },
        selected = currentDestination?.hierarchy?.any { it.route == route } == true,
        onClick = {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    )
}

private val String.stringType
    get() = navArgument(this) {
        nullable = true
        type = NavType.StringType
    }.let(::listOf)

private val NavBackStackEntry.regionId get() = arguments?.getString(regionIdArg)
private val NavBackStackEntry.animalId get() = arguments?.getString(animalIdArg)

sealed class Screen(val route: String, val resource: String) {

    object Catalogue : Screen("catalogue", "catalogue")

    class CatalogueWithRegion(regionId: String) : Screen("catalogue/$regionId", "catalogue")

    object Map : Screen("map", "map")

    object Planner : Screen("planner", "planner")

    object Animal : Screen("animal", "animal")

    companion object {

        const val regionIdArg = "regionId"
        const val animalIdArg = "animalId"
    }
}

val items = listOf(
    Screen.Catalogue,
    Screen.Map,
    Screen.Planner,
)

//@Composable
//fun CatalogueScreen(navController: NavController) {
//    Button(onClick = { navController.navigate(Screen.Planner.route) }) {
//        Text(text = "Go to planner")
//    }
//}

@Composable
fun MapScreen(navController: NavController) {
    Text(text = "MapScreen")
}
