package com.jacekpietras.zoo.map.ui

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.jacekpietras.mapview.ui.compose.MapRenderer
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.map.extensions.getActivity
import com.jacekpietras.zoo.map.model.MapEffect.ShowToast
import com.jacekpietras.zoo.map.router.MapRouterImpl
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import com.jacekpietras.zoo.tracking.permissions.rememberGpsPermissionRequesterState
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MapScreen(
    navController: NavController,
    animalId: String? = null,
    regionId: String? = null,
) {
    val mapRenderer = MapRenderer.OPEN_GL
    val context = LocalContext.current
    val activity = context.getActivity()
    val viewModel = getViewModel<MapViewModel> {
        parametersOf(animalId, regionId, mapRenderer)
    }
    val router = MapRouterImpl({ activity }, navController)
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

    OnPauseListener { viewModel.onStopEvent() }
    BackHandler { viewModel.onBackClicked(router) }

    val viewState by viewModel.viewState.collectAsState(null)

    MapView(
        viewState,
        mapRenderer = mapRenderer,
        onBack = { viewModel.onBackClicked(router) },
        onClose = viewModel::onCloseClicked,
        onLocationClicked = { viewModel.onLocationButtonClicked(permissionChecker) },
        onCameraClicked = { viewModel.onCameraButtonClicked(router) },
        onAnimalClicked = { viewModel.onAnimalClicked(router, it) },
        onRegionClicked = { viewModel.onRegionClicked(router, permissionChecker, it) },
        onSizeChanged = viewModel::onSizeChanged,
        onClick = viewModel::onClick,
        onTransform = viewModel::onTransform,
        onMapActionClicked = viewModel::onMapActionClicked,
        update = { updateCallback -> viewModel.setUpdateCallback(updateCallback) }
    )
}

private fun toast(context: Context, text: RichText) {
    Toast.makeText(context, text.toString(context), Toast.LENGTH_SHORT).show()
}
