package com.jacekpietras.zoo.catalogue.feature.list.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueViewState
import com.jacekpietras.zoo.catalogue.feature.list.router.CatalogueRouterImpl
import com.jacekpietras.zoo.catalogue.feature.list.viewmodel.CatalogueViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CatalogueScreen(
    navController: NavController,
    regionId: String?,
) {

    val viewModel = getViewModel<CatalogueViewModel> { parametersOf(regionId) }

    val router by lazy { CatalogueRouterImpl(navController) }
    val viewState: CatalogueViewState by viewModel.viewState.observeAsState(initial = CatalogueViewState())

    CatalogueView(
        viewState = viewState,
        onBackClicked = { viewModel.onBackClicked(router) },
        onCloseClicked = viewModel::onCloseClicked,
        onSearch = viewModel::onSearch,
        onSearchClicked = viewModel::onSearchClicked,
        onFilterClicked = viewModel::onFilterClicked,
        onAnimalClicked = { animalId ->
            viewModel.onAnimalClicked(
                animalId = animalId,
                router = router
            )
        }
    )
}
