package com.jacekpietras.zoo.map.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.compose.rememberImagePainter
import com.google.android.material.composethemeadapter.MdcTheme
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.ComposableMapView
import com.jacekpietras.mapview.ui.ComposablePaintBaker
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.zoo.core.extensions.observe
import com.jacekpietras.zoo.core.ui.ClosableToolbarView
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.model.MapCarouselItem
import com.jacekpietras.zoo.map.model.MapEffect.*
import com.jacekpietras.zoo.map.model.MapViewState
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
    private val paintBaker by lazy { ComposablePaintBaker(requireActivity()) }
    private val permissionChecker = GpsPermissionRequester(fragment = this)
    private val mapLogic = MapViewLogic(
        invalidate = { mapList.value = it },
        bakeCanvasPaint = { paintBaker.bakeCanvasPaint(it) },
        bakeBorderCanvasPaint = { paintBaker.bakeBorderCanvasPaint(it) },
        setOnPointPlacedListener = { point -> viewModel.onPointPlaced(point) },
    )
    private val mapList = MutableLiveData<List<MapViewLogic.RenderItem<ComposablePaint>>>()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View = ComposeView(requireContext()).apply {
        setContent {
            MdcTheme {
                Column {
                    val viewState by viewModel.viewState.observeAsState(MapViewState())
                    AnimatedVisibility(
                        visibleState = remember { MutableTransitionState(false) }
                            .apply { targetState = viewState.isGuidanceShown },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        var size by remember { mutableStateOf(Size.Zero) }

                        ClosableToolbarView(
                            modifier = Modifier.onSizeChanged {
                                size = it.toSize()
                            },
                            title = viewState.title,
                            isBackArrowShown = viewState.isBackArrowShown,
                            onBack = { viewModel.onBackClicked(router) },
                            onClose = { viewModel.onCloseClicked() },
                        ) {

                            val carouselItemWidth: Dp = (with(LocalDensity.current) { (size.width).toDp() } - 32.dp) / 3.5f

                            AnimatedVisibility(
                                visibleState = remember { MutableTransitionState(viewState.mapCarouselItems.isNotEmpty()) }
                                    .apply { targetState = viewState.mapCarouselItems.isNotEmpty() },
                                modifier = Modifier.fillMaxWidth(),
                                enter = expandVertically(),
                                exit = shrinkVertically(),
                            ) {
                                LazyRow(
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = carouselItemWidth + 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                ) {
                                    items(viewState.mapCarouselItems) { carouselItem ->
                                        when (carouselItem) {
                                            is MapCarouselItem.Animal -> {
                                                AnimalCarouselItem(
                                                    item = carouselItem,
                                                    carouselItemWidth = carouselItemWidth,
                                                    onClick = { viewModel.onAnimalClicked(router, carouselItem.id) },
                                                )
                                            }
                                            is MapCarouselItem.Region -> {
                                                RegionCarouselItem(
                                                    item = carouselItem,
                                                    carouselItemWidth = carouselItemWidth,
                                                    onClick = { viewModel.onRegionClicked(router, carouselItem.id) },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Box(modifier = Modifier) {
                        ComposableMapView(
                            onSizeChanged = mapLogic::onSizeChanged,
                            onClick = mapLogic::onClick,
                            onTransform = mapLogic::onTransform,
                            mapList = mapList.observeAsState(),
                        )
                        Column {
                            AnimatedVisibility(
                                visibleState = remember { MutableTransitionState(viewState.isMapActionsVisible) }
                                    .apply { targetState = viewState.isMapActionsVisible },
                                modifier = Modifier.fillMaxWidth(),
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically(),
                            ) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                ) {
                                    items(viewState.mapActions) { mapAction ->
                                        Card(
                                            shape = RoundedCornerShape(50),
                                        ) {
                                            Box(
                                                modifier = Modifier.clickable {
                                                    viewModel.onMapActionClicked(mapAction)
                                                }
                                            ) {
                                                Text(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    text = stringResource(mapAction.title),
                                                    color = Color.Black,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        UploadButtonView(Modifier.align(Alignment.TopEnd))
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
        }
    }

    @Composable
    private fun RegionCarouselItem(
        item: MapCarouselItem.Region,
        carouselItemWidth: Dp,
        onClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .width(carouselItemWidth)
                .clickable(onClick = onClick),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row {
                AnimalImageView(item.photoUrlLeftTop, carouselItemWidth / 2)
                AnimalImageView(item.photoUrlRightTop, carouselItemWidth / 2)
            }
            Row {
                AnimalImageView(item.photoUrlLeftBottom, carouselItemWidth / 2)
                AnimalImageView(item.photoUrlRightBottom, carouselItemWidth / 2)
            }
            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier,
                text = item.name.toString(LocalContext.current),
                color = Color.Black,
                fontSize = 14.sp,
            )
        }
    }

    @Composable
    private fun imagePainter(url: String?) = rememberImagePainter(
        data = url ?: "no image",
        builder = { crossfade(true) }
    )

    @Composable
    private fun AnimalCarouselItem(
        item: MapCarouselItem.Animal,
        carouselItemWidth: Dp,
        onClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .width(carouselItemWidth)
                .clickable(onClick = onClick),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimalImageView(item.photoUrl, carouselItemWidth)
            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier,
                text = item.name.toString(LocalContext.current),
                color = Color.Black,
                fontSize = 14.sp,
            )
        }
    }

    @Composable
    private fun AnimalImageView(url: String?, size: Dp) {
        Image(
            painter = imagePainter(url),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(size)
                .width(size)
        )
    }

    @Composable
    private fun LocationButtonView(modifier: Modifier = Modifier) {
        FloatingActionButton(
            modifier = modifier,
            onClick = { viewModel.onLocationButtonClicked(permissionChecker) },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_my_location_24),
                tint = Color.Black,
                contentDescription = stringResource(R.string.my_location),
            )
        }
    }

    @Composable
    private fun CameraButtonView(modifier: Modifier = Modifier) {
        FloatingActionButton(
            modifier = modifier,
            onClick = { viewModel.onCameraButtonClicked(router) },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_camera_24),
                tint = Color.Black,
                contentDescription = stringResource(R.string.my_location),
            )
        }
    }

    @Composable
    private fun UploadButtonView(modifier: Modifier = Modifier) {
        FloatingActionButton(
            modifier = modifier
                .padding(16.dp)
                .padding(top = 48.dp),
            onClick = { viewModel.onUploadClicked() },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_upload_24),
                tint = Color.Black,
                contentDescription = stringResource(R.string.upload),
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            mapWorldViewState.observe(viewLifecycleOwner) {
                with(it) {
                    mapLogic.worldData = MapViewLogic.WorldData(
                        bounds = worldBounds,
                        objectList = mapData,
                        terminalPoints = terminalPoints,
                    )
                }
            }
            volatileViewState.observe(viewLifecycleOwner) {
                with(it) {
                    mapLogic.userData = MapViewLogic.UserData(
                        userPosition = userPosition,
                        compass = compass,
                        clickOnWorld = snappedPoint,
                        shortestPath = shortestPath,
                    )
                }
            }

            effect.observe(viewLifecycleOwner) {
                when (it) {
                    is ShowToast -> toast(it.text.toString(requireContext()))
                    is CenterAtUser -> mapLogic.centerAtUserPosition()
                    is CenterAtPoint -> mapLogic.centerAtPoint(it.point)
                }
            }
        }
    }

    private fun toast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}
