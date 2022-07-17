package com.jacekpietras.zoo.catalogue.feature.animal.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
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
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
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
    viewModel.fillColors(ZooTheme.colors.mapColors)
    val router by lazy { AnimalComposeRouterImpl({ activity }, navController) }

    var mapList by remember { mutableStateOf<List<MapViewLogic.RenderItem<ComposablePaint>>>(emptyList()) }
    val mapLogic = remember {
        makeComposableMapLogic(
            activity = activity,
            invalidate = { mapList = it },
        )
    }

    val viewState by viewModel.viewState.collectAsState(null)
    mapLogic.updateMap(viewState)

    AnimalView(
        viewState = viewState,
        mapList = mapList,
        onWebClicked = { viewModel.onWebClicked(router) },
        onWikiClicked = { viewModel.onWikiClicked(router) },
        onNavClicked = { viewModel.onNavClicked(router) },
        onFavoriteClicked = viewModel::onFavoriteClicked,
        onMapSizeChanged = mapLogic::onSizeChanged,
    )
}

private fun makeComposableMapLogic(
    activity: Activity,
    invalidate: (List<MapViewLogic.RenderItem<ComposablePaint>>) -> Unit
): MapViewLogic<ComposablePaint> {
    val paintBaker by lazy { ComposablePaintBaker(activity) }
    return MapViewLogic(
        invalidate = invalidate,
        bakeCanvasPaint = { paintBaker.bakeCanvasPaint(it) },
        bakeBorderCanvasPaint = { paintBaker.bakeBorderCanvasPaint(it) },
        bakeDimension = { paintBaker.bakeDimension(it) },
    )
}

private fun MapViewLogic<ComposablePaint>.updateMap(viewState: AnimalViewState?) {
    if (viewState == null) return

    worldData = MapViewLogic.WorldData(
        bounds = viewState.worldBounds,
        objectList = viewState.mapData,
    )
    setRotate(-23f)
    onScale(0f, 0f, Float.MAX_VALUE)
}

private fun Context.getActivity(): Activity {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    throw IllegalStateException("Activity not available")
}
