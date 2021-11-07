package com.jacekpietras.zoo.catalogue.feature.list.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueViewState
import com.jacekpietras.zoo.catalogue.feature.list.router.CatalogueRouterImpl
import com.jacekpietras.zoo.catalogue.feature.list.viewmodel.CatalogueViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CatalogueFragment : Fragment() {

    private val viewModel by viewModel<CatalogueViewModel>()
    private val router by lazy { CatalogueRouterImpl(findNavController()) }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View = ComposeView(requireContext()).apply {
        setContent {
            val viewState: CatalogueViewState by viewModel.viewState.collectAsState(initial = CatalogueViewState())

            with(viewState) {
                MaterialTheme {
                    Column {
                        ToolbarWithFilters(
                            filterList,
                            filtersVisible,
                            searchVisible,
                            searchText,
                            onSearch = viewModel::onSearch,
                            onSearchClicked = viewModel::onSearchClicked,
                            onFilterClicked = viewModel::onFilterClicked,
                        )
                        AnimalList(
                            animalList = animalList,
                            onAnimalClicked = { animalId ->
                                viewModel.onAnimalClicked(
                                    animalId = animalId,
                                    router = router
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
