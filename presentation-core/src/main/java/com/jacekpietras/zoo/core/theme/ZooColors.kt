@file:Suppress("MemberVisibilityCanBePrivate", "unused", "CanBeParameter")

package com.jacekpietras.zoo.core.theme

import androidx.compose.material.Colors
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.jacekpietras.zoo.core.theme.Palette.secondarySurface

@Immutable
class ZooColors(
    val nightTheme: Boolean = false,
) {

    val primary: Color = Palette.primary onNight Palette.primaryDark
    val onPrimary: Color = Palette.white

    val secondary: Color = Palette.secondary onNight Palette.secondaryDark
    val onSecondary: Color = Palette.white

    val error: Color = Palette.error
    val onError: Color = Palette.white

    val surface: Color = Palette.white onNight Palette.black
    val onSurface: Color = Palette.black onNight Palette.white.copy(alpha = 0.8f)

    val surfaceSecondary: Color = secondarySurface onNight secondarySurface.copy(alpha = 0.1f)

    val textPrimaryOnSurface: Color = onSurface
    val textSecondaryOnSurface: Color = textPrimaryOnSurface.copy(alpha = 0.9f) onNight textPrimaryOnSurface.copy(alpha = 0.7f)
    val textTertiaryOnSurface: Color = textPrimaryOnSurface.copy(alpha = 0.7f) onNight textPrimaryOnSurface.copy(alpha = 0.5f)

    val divider: Color = onSurface.copy(alpha = 0.7f) onNight onSurface.copy(alpha = 0.5f)
    val shimmerBg: Color = Palette.gray200 onNight Palette.gray900
    val shimmerShine: Color = Palette.white onNight Palette.gray800

    val mapColors = MapColors(nightTheme)

    private infix fun <T> T.onNight(right: T): T = if (nightTheme) right else this
}

internal val ZooColors.MaterialColors: Colors
    get() = Colors(
        primary = primary,
        primaryVariant = primary,
        secondary = secondary,
        secondaryVariant = secondary,
        background = surface,
        surface = surface,
        error = error,
        onPrimary = onPrimary,
        onSecondary = onPrimary,
        onBackground = onSurface,
        onSurface = onSurface,
        onError = onError,
        isLight = !nightTheme,
    )
