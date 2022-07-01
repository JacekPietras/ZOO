package com.jacekpietras.zoo.catalogue.feature.animal.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.ComposablePaintBaker
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.router.AnimalComposeRouterImpl
import com.jacekpietras.zoo.catalogue.feature.animal.router.AnimalRouterImpl
import com.jacekpietras.zoo.catalogue.feature.animal.viewmodel.AnimalViewModel
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

    val mapList = MutableLiveData<List<MapViewLogic.RenderItem<ComposablePaint>>>()
    val paintBaker by lazy { ComposablePaintBaker(activity) }
    val mapLogic = MapViewLogic(
        invalidate = { mapList.value = it },
        bakeCanvasPaint = { paintBaker.bakeCanvasPaint(it) },
        bakeBorderCanvasPaint = { paintBaker.bakeBorderCanvasPaint(it) },
        bakeDimension = { paintBaker.bakeDimension(it) },
    )

    val viewState by viewModel.viewState.observeAsState()
    mapLogic.updateMap(viewState)

    AnimalFragmentView(
        viewState = viewState,
        mapList = mapList.observeAsState().value,
        onWebClicked = { viewModel.onWebClicked(router) },
        onWikiClicked = { viewModel.onWikiClicked(router) },
        onNavClicked = { viewModel.onNavClicked(router) },
        onFavoriteClicked = viewModel::onFavoriteClicked,
        onMapSizeChanged = mapLogic::onSizeChanged,
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
