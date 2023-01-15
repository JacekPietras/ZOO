package com.jacekpietras.zoo.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

@Composable
fun MainActivityView() {
    val navController = rememberNavController()
    Scaffold(
        Modifier.navigationBarsPadding(),
        bottomBar = { MainBottomNavigationView(navController) },
        content = { padding ->
            Box(Modifier.padding(padding)) {
                MainNavHost(navController)
            }
        },
    )
}
