package com.jacekpietras.zoo.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalDensity
import androidx.core.view.WindowCompat
import com.jacekpietras.zoo.core.theme.ZooTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ZooTheme {
                MainActivityView()
            }
        }
    }
}
