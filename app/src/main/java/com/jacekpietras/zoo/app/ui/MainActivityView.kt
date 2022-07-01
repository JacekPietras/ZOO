package com.jacekpietras.zoo.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
            composable(Screen.Catalogue.route) { CatalogueScreen(navController) }
            composable(Screen.Map.route) { MapScreen(navController) }
            composable(Screen.Planner.route) { PlannerScreen(navController) }
        }
    }
}

sealed class Screen(val route: String, val resource: String) {
    object Catalogue : Screen("catalogue", "catalogue")
    object Map : Screen("map", "map")
    object Planner : Screen("planner", "planner")
}

val items = listOf(
    Screen.Catalogue,
    Screen.Map,
    Screen.Planner,
)

@Composable
fun CatalogueScreen(navController: NavController) {
    Button(onClick = { navController.navigate(Screen.Planner.route) }) {
        Text(text = "Go to planner")
    }
}

@Composable
fun MapScreen(navController: NavController) {
    Text(text = "MapScreen")
}
