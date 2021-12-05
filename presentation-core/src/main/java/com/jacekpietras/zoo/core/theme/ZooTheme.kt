package com.jacekpietras.zoo.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalAppColors = staticCompositionLocalOf { ZooColors() }

object ZooTheme {

    val colors: ZooColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

@Composable
fun ZooTheme(
    colors: ZooColors = ZooColors(darkTheme = isSystemInDarkTheme()),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppColors provides colors,
    ) {
        MaterialTheme(
            colors = colors.MaterialColors,
            content = content,
        )
    }
}
