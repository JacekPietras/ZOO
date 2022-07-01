package com.jacekpietras.zoo.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun MainActivityView() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { MainBottomNavigationView(navController) },
        content = { padding ->
            Box(Modifier.padding(padding)) {
                MainNavHost(navController)
            }
        },
    )
}

@Composable
fun MapScreen(navController: NavController) {
    Text(text = "MapScreen")
}
