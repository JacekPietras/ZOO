package com.jacekpietras.zoo.scrapper.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.jacekpietras.zoo.scrapper.model.ScrapperState
import com.jacekpietras.zoo.scrapper.viewmodel.ScrapperViewModel
import org.koin.androidx.compose.getViewModel

class ScrapperActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel = getViewModel<ScrapperViewModel>()
            val viewState by viewModel.viewState.collectAsState(initial = ScrapperState())

            AnimalList(viewState.notKnownAnimals)
        }
    }

    @Composable
    private fun AnimalList(notKnownAnimals: List<String>) {
        LazyColumn {
            items(notKnownAnimals) { animal ->
                Text(
                    color = Color.Black,
                    text = animal,
                )
            }
        }
    }
}
