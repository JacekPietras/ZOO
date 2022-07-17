package com.jacekpietras.zoo.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalAppColors = staticCompositionLocalOf { ZooColors() }
private val LocalAppDrawable = staticCompositionLocalOf { ZooDrawable() }

object ZooTheme {

    val colors: ZooColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val drawable: ZooDrawable
        @Composable
        @ReadOnlyComposable
        get() = LocalAppDrawable.current
}

@Composable
fun ZooTheme(
    large: Boolean = false,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    colors: ZooColors = ZooColors(nightTheme = isDarkTheme),
    drawable: ZooDrawable = ZooDrawable(large = large, nightTheme = isDarkTheme),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppDrawable provides drawable,
    ) {
        MaterialTheme(
            colors = colors.MaterialColors,
            content = content,
        )
    }
}
