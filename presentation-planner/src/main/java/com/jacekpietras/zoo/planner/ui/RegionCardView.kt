package com.jacekpietras.zoo.planner.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.planner.R

@Composable
internal fun RegionCardView(
    modifier: Modifier = Modifier,
    isDragged: Boolean,
    title: RichText,
    info: RichText,
    isMutable: Boolean = true,
    isSeen: Boolean = false,
    isRemovable: Boolean = true,
    isFirst: Boolean = true,
    isLast: Boolean = true,
    onRemove: () -> Unit,
    onUnlock: () -> Unit,
    onUnsee: () -> Unit,
) {
    val elevation = if (isDragged) 8.dp else 0.dp
    val elevationState by animateDpAsState(elevation)

    Card(
        modifier = modifier
            .height(IntrinsicSize.Max),
        elevation = elevationState,
        onClick = onUnsee.takeIf { isSeen },
    ) {
        Row(Modifier.fillMaxHeight()) {
            Box(Modifier.fillMaxHeight()) {
                AnimatedDashedLine(isFirst, isDragged, isLast)
                PositionIcon(isSeen)
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = title.toString(LocalContext.current),
                    style = MaterialTheme.typography.subtitle2,
                    color = ZooTheme.colors.textPrimaryOnSurface.dimWhen(isSeen),
                )
                if (info != RichText.Empty) {
                    Text(
                        text = info.toString(LocalContext.current),
                        style = MaterialTheme.typography.body2,
                        color = ZooTheme.colors.textTertiaryOnSurface.dimWhen(isSeen),
                    )
                }
            }

            animateVisibility(!isMutable || isDragged) {
                SideIconView(
                    iconRes = R.drawable.ic_lock_24,
                    contentDescription = R.string.unlock,
                    onClick = onUnlock,
                )
            }

            if (isRemovable) {
                SideIconView(
                    iconRes = R.drawable.ic_close_24,
                    contentDescription = R.string.remove_from_plan,
                    tint = MaterialTheme.colors.onSurface.dimWhen(isSeen),
                    onClick = onRemove,
                )
            }
        }
    }
}

@Composable
private fun PositionIcon(isSeen: Boolean) {
    val iconRes = if (isSeen) {
        R.drawable.ic_visibility_18
    } else {
        R.drawable.ic_trip_circle_18
    }
    Icon(
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
        painter = painterResource(id = iconRes),
        tint = ZooTheme.colors.onSurface.dimWhen(isSeen),
        contentDescription = null // decorative element
    )
}

@Composable
private fun AnimatedDashedLine(isFirst: Boolean, isDragged: Boolean, isLast: Boolean) {
    val cardHeight = remember { mutableStateOf(36.dp) }
    val beginHeight = remember { mutableStateOf(14.dp) }
    val topLine = if (isFirst || isDragged) {
        14.dp
    } else {
        beginHeight.value
    }
    val bottomLine = if (isLast || isDragged) {
        36.dp
    } else {
        cardHeight.value
    }
    val animateTopLine by animateDpAsState(targetValue = topLine)
    val animateBottomLine by animateDpAsState(targetValue = bottomLine)

    DashedLine(animateTopLine, animateBottomLine, onHeightChanged = {
        cardHeight.value = it
        beginHeight.value = 0.dp
    })
}

@Composable
private fun DashedLine(
    topLineStart: Dp,
    bottomLineStart: Dp,
    onHeightChanged: (Dp) -> Unit,
) {
    val dashColor = ZooTheme.colors.onSurface
    Canvas(
        modifier = Modifier
            .padding(start = 25.dp)
            .width(2.dp)
            .fillMaxHeight()
    ) {
        onHeightChanged(size.height.toDp())

        dashedLine(start = topLineStart.toPx(), end = 14.dp.toPx(), color = dashColor)
        dashedLine(start = bottomLineStart.toPx(), end = 36.dp.toPx(), color = dashColor)
    }
}

private fun DrawScope.dashedLine(start: Float, end: Float, color: Color) {
    if (start == end) return

    val size = 4.dp.toPx()
    val offset = if (start < end) {
        size / 2
    } else {
        -size / 2
    }
    drawLine(
        start = Offset(x = 0f, y = start + offset),
        end = Offset(x = 0f, y = end),
        color = color,
        strokeWidth = 2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(
                size,
                size,
            ),
            phase = 0f,
        )
    )
}

private fun Color.dimWhen(dim: Boolean, amount: Float = 0.5f): Color =
    if (dim) {
        this.copy(alpha = this.alpha * amount)
    } else {
        this
    }

@Composable
private fun Card(
    modifier: Modifier = Modifier,
    elevation: Dp = 1.dp,
    onClick: (() -> Unit)?,
    block: @Composable () -> Unit,
) {
    if (onClick == null) {
        Card(
            shape = RoundedCornerShape(8.dp),
            backgroundColor = ZooTheme.colors.surface,
            elevation = elevation,
            modifier = modifier.fillMaxSize(),
            content = block,
        )
    } else {
        Card(
            shape = RoundedCornerShape(8.dp),
            backgroundColor = ZooTheme.colors.surface,
            elevation = elevation,
            modifier = modifier.fillMaxSize(),
            content = block,
            onClick = onClick,
        )
    }
}

@Composable
private inline fun animateVisibility(visible: Boolean, crossinline block: @Composable () -> Unit) {
    AnimatedVisibility(
        visibleState = remember { MutableTransitionState(visible) }.apply { targetState = visible },
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        block()
    }
}

@Composable
private fun SideIconView(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    @StringRes contentDescription: Int,
    tint: Color = MaterialTheme.colors.onSurface,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier
            .then(Modifier.size(48.dp)),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(iconRes),
            tint = tint,
            contentDescription = stringResource(contentDescription)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegionCardViewPreview() {
    RegionCardView(
        isDragged = false,
        title = RichText.Value("Title"),
        info = RichText.Value("some additional information"),
        isMutable = true,
        isSeen = false,
        isRemovable = true,
        onRemove = {},
        onUnlock = {},
        onUnsee = {},
    )
}