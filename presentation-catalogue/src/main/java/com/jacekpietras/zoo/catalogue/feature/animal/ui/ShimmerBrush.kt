package com.jacekpietras.zoo.catalogue.feature.animal.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.jacekpietras.zoo.core.theme.ZooTheme

@Composable
private fun shimmerBrush(
    colors: List<Color> = listOf(ZooTheme.colors.shimmerBg, ZooTheme.colors.shimmerShine, ZooTheme.colors.shimmerBg),
    progress: Float,
    width: Dp,
): Brush {
    val gradientWidth = with(LocalDensity.current) { width.toPx() }
    val normalizedProgress = (progress - 0.5f) * 4
    val xShimmer = gradientWidth * normalizedProgress
    val yShimmer = gradientWidth * normalizedProgress

    return Brush.linearGradient(
        colors,
        start = Offset(xShimmer - gradientWidth, yShimmer - gradientWidth),
        end = Offset(xShimmer + gradientWidth, xShimmer + gradientWidth)
    )
}

@SuppressLint("ComposableModifierFactory")
@Composable
internal fun Modifier.shimmerWhen(progress: Float, width: Dp, condition: () -> Boolean): Modifier {
    return if (condition()) {
        background(
            shimmerBrush(
                progress = progress,
                width = width,
            )
        )
    } else {
        this
    }
}
