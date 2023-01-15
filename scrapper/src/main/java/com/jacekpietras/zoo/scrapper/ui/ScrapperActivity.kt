package com.jacekpietras.zoo.scrapper.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import com.jacekpietras.zoo.scrapper.viewmodel.ScrapperViewModel
import org.koin.androidx.compose.getViewModel

class ScrapperActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val viewModel = getViewModel<ScrapperViewModel>()

            LaunchedEffect("scrapping") {
                viewModel.scrap()
            }
        }
    }
}
