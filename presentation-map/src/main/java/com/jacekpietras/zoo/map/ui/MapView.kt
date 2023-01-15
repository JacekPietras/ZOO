package com.jacekpietras.zoo.map.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.ui.compose.MapComposable
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.core.ui.ClosableToolbarView
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.map.BuildConfig
import com.jacekpietras.zoo.map.model.MapAction
import com.jacekpietras.zoo.map.model.MapViewState

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
    mapList: List<RenderItem<ComposablePaint>>,
) {
    Box {
        MapComposable(
            modifier = Modifier.fillMaxSize(),
            backgroundColor = ZooTheme.colors.mapColors.colorMapGrass,
            onSizeChanged = onSizeChanged,
            onClick = onClick,
            onTransform = onTransform,
            mapList = mapList,
        )
        MapToolbar(
            viewState = viewState,
            onBack = onBack,
            onClose = onClose,
            onAnimalClicked = onAnimalClicked,
            onRegionClicked = onRegionClicked,
        )
        Column {
            val paddingOnChips = if (viewState?.isNavigationVisible == true) {
                0.dp
            } else {
                WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
            }

            PlannerNavigationToolbar(
                isNavigationVisible = viewState?.isNavigationVisible ?: false,
                navigationText = viewState?.navigationText ?: RichText.Empty,
            )

            ActionChips(
                isVisible = viewState?.isMapActionsVisible ?: false,
                mapActions = viewState?.mapActions ?: emptyList(),
                onMapActionClicked = onMapActionClicked,
                topPadding = paddingOnChips,
            )
        }
        ActionButtons(
            modifier = Modifier
                .align(Alignment.BottomEnd),
            onCameraClicked = onCameraClicked,
            onLocationClicked = onLocationClicked,
        )
        if (BuildConfig.DEBUG) {
            Text(
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.BottomCenter),
                text = ("l:" + viewState?.luminanceText),
                style = MaterialTheme.typography.caption,
                color = ZooTheme.colors.textPrimaryOnSurface,
            )
        }
    }
}

@Composable
fun PlannerNavigationToolbar(isNavigationVisible: Boolean, navigationText: RichText) {
    val statusBarsPadding = statusBarPaddingInt()
    AnimatedVisibility(
        visibleState = remember { MutableTransitionState(isNavigationVisible) }
            .apply { targetState = isNavigationVisible },
        modifier = Modifier.fillMaxWidth(),
        enter = slideInVertically(
            initialOffsetY = { -it - statusBarsPadding }
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it - statusBarsPadding }
        ),
    ) {
        Card(
            shape = RectangleShape,
            backgroundColor = ZooTheme.colors.surface,
            elevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .defaultMinSize(minHeight = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = navigationText.toString(LocalContext.current),
                    color = MaterialTheme.colors.onSurface,
                )
            }
        }
    }
}

@Composable
private fun statusBarPaddingInt(): Int =
    with(LocalDensity.current) { WindowInsets.systemBars.asPaddingValues().calculateTopPadding().toPx() }
        .toInt()

@Composable
private fun MapToolbar(
    viewState: MapViewState?,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onAnimalClicked: (AnimalId) -> Unit,
    onRegionClicked: (RegionId) -> Unit
) {
    if (viewState == null) return

    AnimatedVisibility(
        visibleState = remember { MutableTransitionState(false) }
            .apply { targetState = viewState.isGuidanceShown },
        modifier = Modifier.fillMaxWidth(),
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
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
}