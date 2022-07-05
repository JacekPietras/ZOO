package com.jacekpietras.zoo.map.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.ComposableMapView
import com.jacekpietras.mapview.ui.ComposablePaintBaker
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.zoo.map.utils.observe
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.core.ui.ClosableToolbarView
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.model.MapAction
import com.jacekpietras.zoo.map.model.MapEffect.CenterAtPoint
import com.jacekpietras.zoo.map.model.MapEffect.CenterAtUser
import com.jacekpietras.zoo.map.model.MapEffect.ShowToast
import com.jacekpietras.zoo.map.model.MapViewState
import com.jacekpietras.zoo.map.model.MapVolatileViewState
import com.jacekpietras.zoo.map.model.MapWorldViewState
import com.jacekpietras.zoo.map.router.MapRouterImpl
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import com.jacekpietras.zoo.tracking.permissions.rememberGpsPermissionRequesterState
import kotlinx.coroutines.flow.MutableStateFlow
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
    val router = MapRouterImpl({ activity }, navController)
    val permissionChecker = rememberGpsPermissionRequesterState()

    val mapList = MutableStateFlow<List<MapViewLogic.RenderItem<ComposablePaint>>>(emptyList())
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
    val context = LocalContext.current

    viewModel.mapWorldViewState.observe(LocalLifecycleOwner.current) { mapLogic.applyToMap(it) }
    viewModel.volatileViewState.observe(LocalLifecycleOwner.current) { mapLogic.applyToMap(it) }

    viewModel.effect.observe(LocalLifecycleOwner.current) {
        when (it) {
            is ShowToast -> toast(context, it.text)
            is CenterAtUser -> mapLogic.centerAtUserPosition()
            is CenterAtPoint -> mapLogic.centerAtPoint(it.point)
        }
    }

    setDefaultNightMode(viewState?.isNightThemeSuggested)

    OnPauseListener { viewModel.onStopEvent() }

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
        mapList = mapList.collectAsState().value,
    )
}

@Composable
private fun setDefaultNightMode(nightTheme: Boolean?) {
    setDefaultNightMode(if (nightTheme == true) MODE_NIGHT_YES else MODE_NIGHT_FOLLOW_SYSTEM)
}

@Composable
private fun MapView(
    viewState: MapViewState?,
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
    mapList: List<MapViewLogic.RenderItem<ComposablePaint>>,
) {
    if (viewState == null) return

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
                mapList = mapList,
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

private fun toast(context: Context, text: RichText) {
    Toast.makeText(context, text.toString(context), Toast.LENGTH_SHORT).show()
}

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

@Composable
private fun OnPauseListener(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onPause: () -> Unit,
) {
    val currentOnPause by rememberUpdatedState(onPause)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                currentOnPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

