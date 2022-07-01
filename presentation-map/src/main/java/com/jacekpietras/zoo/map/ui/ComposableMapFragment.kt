package com.jacekpietras.zoo.map.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.*
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.ComposableMapView
import com.jacekpietras.mapview.ui.ComposablePaintBaker
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.zoo.core.extensions.observe
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.core.ui.ClosableToolbarView
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.model.MapEffect.*
import com.jacekpietras.zoo.map.model.MapViewState
import com.jacekpietras.zoo.map.model.MapVolatileViewState
import com.jacekpietras.zoo.map.model.MapWorldViewState
import com.jacekpietras.zoo.map.router.MapRouterImpl
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import com.jacekpietras.zoo.tracking.GpsPermissionRequester
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ComposableMapFragment : Fragment() {

    private val args: ComposableMapFragmentArgs? by navArgs()
    private val viewModel by viewModel<MapViewModel> {
        parametersOf(args?.animalId, args?.regionId)
    }
    private val router by lazy { MapRouterImpl(findNavController()) }
    private val permissionChecker = GpsPermissionRequester(fragment = this)

    private val paintBaker by lazy { ComposablePaintBaker(requireActivity()) }
    private val mapLogic = MapViewLogic(
        invalidate = { mapList.value = it },
        bakeCanvasPaint = { paintBaker.bakeCanvasPaint(it) },
        bakeBorderCanvasPaint = { paintBaker.bakeBorderCanvasPaint(it) },
        bakeDimension = { paintBaker.bakeDimension(it) },
        setOnPointPlacedListener = { viewModel.onPointPlaced(it) },
        onStopCentering = { viewModel.onStopCentering() },
        onStartCentering = { viewModel.onStartCentering() },
    )
    private val mapList = MutableLiveData<List<MapViewLogic.RenderItem<ComposablePaint>>>()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View = ComposeView(requireContext()).apply {
        setContent {
            val viewState by viewModel.viewState.observeAsState()
            setDefaultNightMode(if (viewState?.isNightThemeSuggested == true) MODE_NIGHT_YES else MODE_NIGHT_FOLLOW_SYSTEM)

            ZooTheme {
                viewState?.let { MapScreen(it) }
            }
        }
    }

    @Composable
    private fun MapScreen(viewState: MapViewState) {
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
                    onBack = { viewModel.onBackClicked(router) },
                    onClose = { viewModel.onCloseClicked() },
                ) {

                    val carouselItemWidth: Dp = (with(LocalDensity.current) { (size.width).toDp() } - 32.dp) / 3.5f
                    ImageCarouselView(
                        viewState.mapCarouselItems,
                        carouselItemWidth,
                        { viewModel.onAnimalClicked(router, it) },
                        { viewModel.onRegionClicked(router, it) },
                    )
                }
            }
            Box(modifier = Modifier) {
                ComposableMapView(
                    modifier = Modifier.fillMaxSize(),
                    onSizeChanged = mapLogic::onSizeChanged,
                    onClick = mapLogic::onClick,
                    onTransform = mapLogic::onTransform,
                    mapList = mapList.observeAsState().value,
                )
                MapActionChips(
                    isVisible = viewState.isMapActionsVisible,
                    mapActions = viewState.mapActions,
                    viewModel::onMapActionClicked,
                )
                Column(
                    Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    CameraButtonView(Modifier.padding(bottom = 16.dp))
                    LocationButtonView()
                }
            }
        }
    }

    @Composable
    private fun LocationButtonView(modifier: Modifier = Modifier) {
        FloatingActionButton(
            modifier = modifier,
            onClick = { viewModel.onLocationButtonClicked(permissionChecker) },
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
    private fun CameraButtonView(modifier: Modifier = Modifier) {
        FloatingActionButton(
            modifier = modifier,
            onClick = { viewModel.onCameraButtonClicked(router) },
            backgroundColor = ZooTheme.colors.surface,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_camera_24),
                tint = MaterialTheme.colors.onSurface,
                contentDescription = stringResource(R.string.my_location),
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            mapWorldViewState.observe(viewLifecycleOwner) { it.applyToMap() }
            volatileViewState.observe(viewLifecycleOwner) { it.applyToMap() }

            effect.observe(viewLifecycleOwner) {
                when (it) {
                    is ShowToast -> toast(it.text.toString(requireContext()))
                    is CenterAtUser -> mapLogic.centerAtUserPosition()
                    is CenterAtPoint -> mapLogic.centerAtPoint(it.point)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onStopEvent()
    }

    private fun toast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun MapWorldViewState.applyToMap() {
        mapLogic.worldData = MapViewLogic.WorldData(
            bounds = worldBounds,
            objectList = mapData,
        )
    }

    private fun MapVolatileViewState.applyToMap() {
        mapLogic.userData = MapViewLogic.UserData(
            userPosition = userPosition,
            compass = compass,
            objectList = mapData,
        )
    }
}
