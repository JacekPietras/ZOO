package com.jacekpietras.zoo.core.ui

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.jacekpietras.zoo.core.R
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.core.theme.ZooTheme
import kotlin.math.roundToInt

private const val INITIAL = 0
private const val SWIPED = 1

@ExperimentalMaterialApi
@Composable
fun ClosableToolbarView(
    modifier: Modifier = Modifier,
    title: Text,
    isBackArrowShown: Boolean = true,
    isSwipable: Boolean = false,
    onBack: () -> Unit = {},
    onClose: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Card(
        shape = RectangleShape,
        backgroundColor = ZooTheme.colors.surface,
        elevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .applySwipeable(
                isSwipable = isSwipable,
                onClose = onClose,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            ClosableRowView(
                isBackArrowShown = isBackArrowShown,
                onBack = onBack,
                title = title,
                onClose = onClose
            )
            content.invoke(this)
        }
    }
}

@SuppressLint("UnnecessaryComposedModifier")
@ExperimentalMaterialApi
private fun Modifier.applySwipeable(
    isSwipable: Boolean,
    onClose: () -> Unit,
): Modifier =
    if (isSwipable) {
        applySwipeable(onClose)
    } else {
        this
    }

@ExperimentalMaterialApi
private fun Modifier.applySwipeable(
    onClose: () -> Unit,
): Modifier = composed {
    var size by remember { mutableStateOf(Size.Zero) }
    val swipeableState = rememberSwipeableState(0)
    val height = if (size.height == 0f) 1f else -size.height

    if (swipeableState.isAnimationRunning) {
        DisposableEffect(Unit) {
            onDispose {
                if (swipeableState.currentValue == SWIPED) {
                    onClose()
                }
            }
        }
    }

    onSizeChanged {
        size = it.toSize()
    }
        .swipeable(
            state = swipeableState,
            anchors = mapOf(height to SWIPED, 0f to INITIAL),
            thresholds = { _, _ -> FractionalThreshold(0.4f) },
            orientation = Orientation.Vertical
        )
        .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
}

@Composable
fun ClosableRowView(
    title: Text,
    isBackArrowShown: Boolean = true,
    onBack: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isBackArrowShown) {
            SideIconView(
                modifier = Modifier.align(Alignment.TopStart),
                iconRes = R.drawable.ic_arrow_back_24,
                contentDescription = R.string.back,
                onClick = onBack,
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .defaultMinSize(minHeight = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = title.toString(LocalContext.current),
                color = MaterialTheme.colors.onSurface,
            )
        }
        SideIconView(
            modifier = Modifier.align(Alignment.TopEnd),
            iconRes = R.drawable.ic_close_24,
            contentDescription = R.string.close,
            onClick = onClose,
        )
    }
}

@Composable
private fun SideIconView(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    @StringRes contentDescription: Int,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier
            .then(Modifier.size(48.dp)),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(iconRes),
            tint = MaterialTheme.colors.onSurface,
            contentDescription = stringResource(contentDescription)
        )
    }
}
