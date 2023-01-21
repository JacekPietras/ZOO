package com.jacekpietras.zoo.catalogue.feature.animal.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.compose.ComposablePaintBaker
import com.jacekpietras.zoo.catalogue.feature.animal.extensions.getActivity
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalEffect.ShowToast
import com.jacekpietras.zoo.catalogue.feature.animal.router.AnimalComposeRouterImpl
import com.jacekpietras.zoo.catalogue.feature.animal.viewmodel.AnimalViewModel
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.tracking.permissions.rememberGpsPermissionRequesterState
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AnimalScreen(
    navController: NavController,
    animalId: String?,
) {
    val context = LocalContext.current
    val activity = context.getActivity()
    val viewModel = getViewModel<AnimalViewModel> {
        parametersOf(animalId, ComposablePaintBaker(context))
    }
    val router by lazy { AnimalComposeRouterImpl({ activity }, navController) }
    val permissionChecker = rememberGpsPermissionRequesterState()

    val colors = ZooTheme.colors.mapColors
    LaunchedEffect("colors" + ZooTheme.isNightMode) {
        viewModel.fillColors(colors)
    }
    LaunchedEffect("effects" + ZooTheme.isNightMode) {
        viewModel.effects.collect {
            when (val effect = viewModel.consumeEffect()) {
                is ShowToast -> toast(context, effect.text)
            }
        }
    }

    val viewState by viewModel.viewState.collectAsState(null)

    AnimalView(
        viewState = viewState,
        onWebClicked = { viewModel.onWebClicked(router) },
        onWikiClicked = { viewModel.onWikiClicked(router) },
        onNavClicked = { viewModel.onNavClicked(router, permissionChecker = permissionChecker) },
        onFavoriteClicked = viewModel::onFavoriteClicked,
        onMapSizeChanged = viewModel::onSizeChanged,
        update = { updateCallback -> viewModel.setUpdateCallback(updateCallback) }
    )
}

private fun toast(context: Context, text: RichText) {
    Toast.makeText(context, text.toString(context), Toast.LENGTH_SHORT).show()
}
