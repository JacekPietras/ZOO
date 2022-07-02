package com.jacekpietras.zoo.map.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.ComposableMapView
import com.jacekpietras.mapview.ui.ComposablePaintBaker
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.core.ui.ClosableToolbarView
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.model.MapAction
import com.jacekpietras.zoo.map.model.MapViewState
import com.jacekpietras.zoo.map.model.MapVolatileViewState
import com.jacekpietras.zoo.map.model.MapWorldViewState
import com.jacekpietras.zoo.map.router.MapComposeRouterImpl
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import com.jacekpietras.zoo.tracking.permissions.GpsPermissionRequester
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MapScreen(
    navController: NavController,
    animalId: String? = null,
    regionId: String? = null,
) {

    val activity = LocalContext.current.getActivity()
    val viewModel = getViewModel<MapViewModel> { parametersOf(animalId, regionId) }
    val router by lazy { MapComposeRouterImpl({ activity }, navController) }
//    val permissionChecker = GpsPermissionRequester(fragment = this)

    val mapList = MutableLiveData<List<MapViewLogic.RenderItem<ComposablePaint>>>()
    val paintBaker by lazy { ComposablePaintBaker(activity) }
    val mapLogic = MapViewLogic(
        invalidate = { mapList.value = it },
        bakeCanvasPaint = { paintBaker.bakeCanvasPaint(it) },
        bakeBorderCanvasPaint = { paintBaker.bakeBorderCanvasPaint(it) },
        bakeDimension = { paintBaker.bakeDimension(it) },
        setOnPointPlacedListener = { viewModel.onPointPlaced(it) },
        onStopCentering = { viewModel.onStopCentering() },
        onStartCentering = { viewModel.onStartCentering() },
    )

    val viewState by viewModel.viewState.observeAsState()
    setDefaultNightMode(if (viewState?.isNightThemeSuggested == true) MODE_NIGHT_YES else MODE_NIGHT_FOLLOW_SYSTEM)

    viewState?.let { viewState2 ->
        MapView(
            viewState2,
            onBack = { viewModel.onBackClicked(router) },
            onClose = viewModel::onCloseClicked,
            onLocationClicked = {},//{ viewModel.onLocationButtonClicked(permissionChecker) }, // fixme implement permission checker
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
}

@Composable
private fun MapView(
    viewState: MapViewState,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onLocationClicked: () -> Unit,
    onCameraClicked: () -> Unit,
    onAnimalClicked: (AnimalId) -> Unit,
    onRegionClicked: (RegionId) -> Unit,
    onSizeChanged: (Int, Int) -> Unit,
    onClick: (Float, Float) -> Unit,
    onTransform: (Float, Float, Float, Float, Float, Float) -> Unit,
    onMapActionClicked: (MapAction) -> Unit,
    mapList: MutableLiveData<List<MapViewLogic.RenderItem<ComposablePaint>>>,
) {
    Column {
        AnimatedVisibility(
            visibleState = remember { MutableTransitionState(false) }
                .apply { targetState = viewState.isGuidanceShown },
            modifier = Modifier.fillMaxWidth(),
        ) {
            var size by remember { mutableStateOf(Size.Zero) }

            ClosableToolbarView(
                modifier = Modifier
                    .onSizeChanged {
                        size = it.toSize()
                    },
                isSwipable = true,
                title = viewState.title,
                isBackArrowShown = viewState.isBackArrowShown,
                onBack = onBack,
                onClose = onClose,
            ) {

                val carouselItemWidth: Dp = (with(LocalDensity.current) { (size.width).toDp() } - 32.dp) / 3.5f
                ImageCarouselView(
                    viewState.mapCarouselItems,
                    carouselItemWidth,
                    onAnimalClicked = onAnimalClicked,
                    onRegionClicked = onRegionClicked,
                )
            }
        }
        Box(modifier = Modifier) {
            ComposableMapView(
                modifier = Modifier.fillMaxSize(),
                onSizeChanged = onSizeChanged,
                onClick = onClick,
                onTransform = onTransform,
                mapList = mapList.observeAsState().value,
            )
            MapActionChips(
                isVisible = viewState.isMapActionsVisible,
                mapActions = viewState.mapActions,
                onMapActionClicked = onMapActionClicked,
            )
            Column(
                Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)
            ) {
                CameraButtonView(Modifier.padding(bottom = 16.dp), onClick = onCameraClicked)
                LocationButtonView(onClick = onLocationClicked)
            }
        }
    }
}

@Composable
private fun LocationButtonView(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        backgroundColor = ZooTheme.colors.surface,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_my_location_24),
            tint = MaterialTheme.colors.onSurface,
            contentDescription = stringResource(R.string.my_location),
        )
    }
}

@Composable
private fun CameraButtonView(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        backgroundColor = ZooTheme.colors.surface,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_camera_24),
            tint = MaterialTheme.colors.onSurface,
            contentDescription = stringResource(R.string.my_location),
        )
    }
}

// fixme implement them:
//override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//    super.onViewCreated(view, savedInstanceState)
//
//    with(viewModel) {
//        mapWorldViewState.observe(viewLifecycleOwner) { mapLogic.applyToMap(it) }
//        volatileViewState.observe(viewLifecycleOwner) { mapLogic.applyToMap(it) }
//
//        effect.observe(viewLifecycleOwner) {
//            when (it) {
//                is ShowToast -> toast(it.text.toString(requireContext()))
//                is CenterAtUser -> mapLogic.centerAtUserPosition()
//                is CenterAtPoint -> mapLogic.centerAtPoint(it.point)
//            }
//        }
//    }
//}
//
//override fun onPause() {
//    super.onPause()
//    viewModel.onStopEvent()
//}

//private fun toast(text: String) {
//    Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
//}

private fun MapViewLogic<ComposablePaint>.applyToMap(viewState: MapWorldViewState) {
    worldData = MapViewLogic.WorldData(
        bounds = viewState.worldBounds,
        objectList = viewState.mapData,
    )
}

private fun MapViewLogic<ComposablePaint>.applyToMap(viewState: MapVolatileViewState) {
    userData = MapViewLogic.UserData(
        userPosition = viewState.userPosition,
        compass = viewState.compass,
        objectList = viewState.mapData,
    )
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
