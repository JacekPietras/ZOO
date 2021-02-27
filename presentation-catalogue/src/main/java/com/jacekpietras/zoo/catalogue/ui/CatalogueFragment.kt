package com.jacekpietras.zoo.catalogue.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.jacekpietras.zoo.catalogue.viewmodel.CatalogueViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CatalogueFragment : Fragment() {

    private val viewModel by viewModel<CatalogueViewModel>()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        ComposeView(requireContext()).apply {
            setContent {
                AnimalList()
            }
        }

    @Composable
    fun AnimalList() {
        val animals: List<String> by viewModel.viewState.collectAsState(initial = emptyList())

        LazyColumn {
            items(animals) { animal ->
                Text(text = animal)
            }
        }
    }
}
