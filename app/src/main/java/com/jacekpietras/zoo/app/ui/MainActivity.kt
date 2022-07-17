package com.jacekpietras.zoo.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.jacekpietras.zoo.app.viewmodel.MainViewModel
import com.jacekpietras.zoo.core.theme.ZooTheme
import org.koin.androidx.compose.getViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val viewModel = getViewModel<MainViewModel>()
            val viewState by viewModel.viewState.collectAsState(null)

            ZooTheme(isDarkTheme = viewState?.isNightModeSuggested == true || isSystemInDarkTheme()) {
                MainActivityView()
            }
        }
    }
}
