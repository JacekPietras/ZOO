package com.jacekpietras.zoo.map.ui

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.ComposablePaintBaker
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.mapview.ui.MapViewLogic.RenderItem
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.map.extensions.applyToMap
import com.jacekpietras.zoo.map.extensions.getActivity
import com.jacekpietras.zoo.map.model.MapEffect.CenterAtUser
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
    val context = LocalContext.current
    val activity = context.getActivity()
    val viewModel = getViewModel<MapViewModel> { parametersOf(animalId, regionId) }
    val router = MapRouterImpl({ activity }, navController)
    val permissionChecker = rememberGpsPermissionRequesterState()

    var mapList by remember { mutableStateOf<List<RenderItem<ComposablePaint>>>(emptyList()) }
    val mapLogic = remember {
        makeComposableMapLogic(
            activity = activity,
            viewModel = viewModel,
            invalidate = { mapList = it },
        )
    }

    LaunchedEffect("mapWorld") {
        viewModel.mapWorldViewState.collect(mapLogic::applyToMap)
    }
    LaunchedEffect("volatile") {
        viewModel.volatileViewState.collect(mapLogic::applyToMap)
    }
    LaunchedEffect("effects") {
        viewModel.effects.collect {
            when (val effect = viewModel.consumeEffect()) {
                is ShowToast -> toast(context, effect.text)
                is CenterAtUser -> mapLogic.centerAtUserPosition()
            }
        }
    }

    OnPauseListener { viewModel.onStopEvent() }

    val viewState by viewModel.viewState.collectAsState(null)

    // fixme dont work without AppCompat Activity
    setDefaultNightMode(viewState?.isNightThemeSuggested)

    MapView(
        viewState,
        onBack = { viewModel.onBackClicked(router) },
        onClose = viewModel::onCloseClicked,
        onLocationClicked = { viewModel.onLocationButtonClicked(permissionChecker) },
        onCameraClicked = { viewModel.onCameraButtonClicked(router) },
        onAnimalClicked = { viewModel.onAnimalClicked(router, it) },
        onRegionClicked = { viewModel.onRegionClicked(router, it) },
        onSizeChanged = mapLogic::onSizeChanged,
        onClick = mapLogic::onClick,
        onTransform = mapLogic::onTransform,
        onMapActionClicked = viewModel::onMapActionClicked,
        mapList = mapList,
    )
}

private fun makeComposableMapLogic(
    activity: Activity,
    viewModel: MapViewModel,
    invalidate: (List<RenderItem<ComposablePaint>>) -> Unit
): MapViewLogic<ComposablePaint> {
    val paintBaker by lazy { ComposablePaintBaker(activity) }
    return MapViewLogic(
        invalidate = invalidate,
        bakeCanvasPaint = { paintBaker.bakeCanvasPaint(it) },
        bakeBorderCanvasPaint = { paintBaker.bakeBorderCanvasPaint(it) },
        bakeDimension = { paintBaker.bakeDimension(it) },
        setOnPointPlacedListener = { viewModel.onPointPlaced(it) },
        onStopCentering = { viewModel.onStopCentering() },
        onStartCentering = { viewModel.onStartCentering() },
    )
}

@Composable
private fun setDefaultNightMode(nightTheme: Boolean?) {
    setDefaultNightMode(if (nightTheme == true) MODE_NIGHT_YES else MODE_NIGHT_FOLLOW_SYSTEM)
}

private fun toast(context: Context, text: RichText) {
    Toast.makeText(context, text.toString(context), Toast.LENGTH_SHORT).show()
}
