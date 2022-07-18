package com.jacekpietras.zoo.catalogue.feature.animal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.jacekpietras.zoo.catalogue.feature.animal.extensions.getActivity
import com.jacekpietras.zoo.catalogue.feature.animal.router.AnimalComposeRouterImpl
import com.jacekpietras.zoo.catalogue.feature.animal.viewmodel.AnimalViewModel
import com.jacekpietras.zoo.core.theme.ZooTheme
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AnimalScreen(
    navController: NavController,
    animalId: String?,
) {
    val activity = LocalContext.current.getActivity()
    val viewModel = getViewModel<AnimalViewModel> { parametersOf(animalId) }
    val router by lazy { AnimalComposeRouterImpl({ activity }, navController) }

    val colors = ZooTheme.colors.mapColors
    LaunchedEffect("colors" + ZooTheme.isNightMode) {
        viewModel.fillColors(colors)
    }

    val viewState by viewModel.viewState.collectAsState(null)
    val mapList by viewModel.mapList.collectAsState(initial = emptyList())

    AnimalView(
        viewState = viewState,
        mapList = mapList,
        onWebClicked = { viewModel.onWebClicked(router) },
        onWikiClicked = { viewModel.onWikiClicked(router) },
        onNavClicked = { viewModel.onNavClicked(router) },
        onFavoriteClicked = viewModel::onFavoriteClicked,
        onMapSizeChanged = viewModel::onSizeChanged,
    )
}
