package com.jacekpietras.zoo.app.ui

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
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jacekpietras.zoo.app.ui.Screen.Companion.regionIdArg
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
                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                        label = { Text(screen.resource) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Map.route, Modifier.padding(innerPadding)) {
            composable(
                Screen.Catalogue.route,
                arguments = listOf(navArgument(regionIdArg) {
                    nullable = true
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                CatalogueScreen(navController, backStackEntry.arguments?.getString(regionIdArg))
            }
            composable(Screen.Map.route) { MapScreen(navController) }
            composable(Screen.Planner.route) { PlannerScreen(navController) }
//            composable(Screen.Animal.route) { AnimalScreen(navController) }
        }
    }
}

sealed class Screen(val route: String, val resource: String) {

    object Catalogue : Screen("catalogue", "catalogue")
    class CatalogueWithRegion(regionId: String) : Screen("catalogue/$regionId", "catalogue")

    object Map : Screen("map", "map")

    object Planner : Screen("planner", "planner")

    object Animal : Screen("animal", "animal")

    companion object {

        const val regionIdArg = "regionId"
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
