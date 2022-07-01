package com.jacekpietras.zoo.app.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
internal fun MainBottomNavigationView(navController: NavController) {
    val items = listOf(
        Screen.Catalogue,
        Screen.Map,
        Screen.Planner,
    )

    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { screen ->
            BottomNavigationItemView(screen.route, screen.resource, currentDestination, navController)
        }
    }
}

@Composable
private fun RowScope.BottomNavigationItemView(
    route: String,
    name: String,
    currentDestination: NavDestination?,
    navController: NavController
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
