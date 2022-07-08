package com.jacekpietras.zoo.map.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.ComposableMapView
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.zoo.core.ui.ClosableToolbarView
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.map.model.MapAction
import com.jacekpietras.zoo.map.model.MapViewState
import timber.log.Timber

@Composable
internal fun MapView(
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

    Timber.e("dupa mapView recomposed")

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
            ActionChips(
                isVisible = viewState.isMapActionsVisible,
                mapActions = viewState.mapActions,
                onMapActionClicked = onMapActionClicked,
            )
            ActionButtons(
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                onCameraClicked = onCameraClicked,
                onLocationClicked = onLocationClicked,
            )
        }
    }
}