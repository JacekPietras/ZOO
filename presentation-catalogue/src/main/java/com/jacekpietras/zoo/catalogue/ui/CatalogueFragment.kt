package com.jacekpietras.zoo.catalogue.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.jacekpietras.zoo.catalogue.viewmodel.CatalogueViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CatalogueFragment : Fragment() {

    private val viewModel by viewModel<CatalogueViewModel>()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
//                Row {
//                    FiltersBar()
                    AnimalList()
//                }
            }
        }
    }

    @Composable
    fun FiltersBar() {
        Column {
            for (i in 0..10)
                Text(text = "Hello world.")
        }
    }

    @Composable
    fun AnimalList() {
//        val livedata: LiveData<List<String>> = viewModel.viewState.asLiveData()
//        val animals: List<String> by livedata.observeAsState(listOf())
        val animals: List<String> = viewModel.animalListExplicit

        LazyColumn {
            items(animals) { animal ->
                Text(text = animal)
            }
        }
    }
}
