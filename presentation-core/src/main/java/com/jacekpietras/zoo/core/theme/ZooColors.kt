@file:Suppress("MemberVisibilityCanBePrivate", "unused", "CanBeParameter")

package com.jacekpietras.zoo.core.theme

import androidx.compose.material.Colors
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
class ZooColors(
    val darkTheme: Boolean = false,
) {

    val primary: Color = if (darkTheme) Palette.primaryDark else Palette.primary
    val onPrimary: Color = Palette.white

    val secondary: Color = if (darkTheme) Palette.secondaryDark else Palette.secondary
    val onSecondary: Color = Palette.white

    val error: Color = Palette.error
    val onError: Color = Palette.white

    val surface: Color = if (darkTheme) Palette.black else Palette.white
    val onSurface: Color = if (darkTheme) Palette.white.copy(alpha = 0.8f) else Palette.black

    val textPrimaryOnSurface: Color = onSurface
    val textSecondaryOnSurface: Color = if (darkTheme) textPrimaryOnSurface.copy(alpha = 0.7f) else textPrimaryOnSurface.copy(alpha = 0.9f)
    val textTertiaryOnSurface: Color = if (darkTheme) textPrimaryOnSurface.copy(alpha = 0.5f) else textPrimaryOnSurface.copy(alpha = 0.7f)

    val divider: Color = if (darkTheme) onSurface.copy(alpha = 0.5f) else onSurface.copy(alpha = 0.7f)
    val shimmerBg: Color = if (darkTheme) Palette.gray900 else Palette.gray200
    val shimmerShine: Color = if (darkTheme) Palette.gray800 else Palette.white
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
        isLight = !darkTheme,
    )
