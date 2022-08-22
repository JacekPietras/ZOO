package com.jacekpietras.zoo.app.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
            BottomNavigationItemView(screen.route, screen.title, screen.iconRes, currentDestination, navController)
        }
    }
}

@Composable
private fun RowScope.BottomNavigationItemView(
    route: String,
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    currentDestination: NavDestination?,
    navController: NavController
) {
    BottomNavigationItem(
        icon = { Icon(painterResource(iconRes), contentDescription = null) },
        label = { Text(stringResource(titleRes)) },
        selected = currentDestination?.hierarchy?.any { it.route == route } == true,
        onClick = {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    )
}
