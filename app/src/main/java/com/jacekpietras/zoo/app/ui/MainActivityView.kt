package com.jacekpietras.zoo.app.ui

import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun MainActivityView() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { MainBottomNavigationView(navController) },
        content = { MainNavHost(navController) },
    )
}

@Composable
fun Catalogue2Screen(navController: NavController) {
    Button(onClick = { navController.navigate(Screen.Planner.route) }) {
        Text(text = "Go to planner")
    }
}

@Composable
fun MapScreen(navController: NavController) {
    Text(text = "MapScreen")
}
