package com.jacekpietras.mapview.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.ui.compose.MapRenderer.COMPOSE
import com.jacekpietras.mapview.ui.compose.MapRenderer.CUSTOM_VIEW
import com.jacekpietras.mapview.ui.compose.MapRenderer.OPEN_GL
import com.jacekpietras.mapview.ui.compose.MapRenderer.SURFACE_VIEW
import com.jacekpietras.mapview.utils.hasGLES20

@Composable
fun UniversalMapComposable(
    mapRenderer: MapRenderer,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onSizeChanged: (Int, Int) -> Unit,
    onClick: ((Float, Float) -> Unit)? = null,
    onTransform: ((Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    update: ((List<RenderItem<Any>>) -> Unit) -> Unit,
) {
    when (mapRenderer) {
        CUSTOM_VIEW -> {
            MapCustomViewComposable(
                modifier = modifier,
                backgroundColor = backgroundColor,
                onSizeChanged = onSizeChanged,
                onClick = onClick,
                onTransform = onTransform,
                update = update,
            )
        }
        SURFACE_VIEW -> {
            MapSurfaceViewComposable(
                modifier = modifier,
                backgroundColor = backgroundColor,
                onSizeChanged = onSizeChanged,
                onClick = onClick,
                onTransform = onTransform,
                update = update,
            )
        }
        COMPOSE -> {
            MapComposable(
                modifier = modifier,
                backgroundColor = backgroundColor,
                onSizeChanged = onSizeChanged,
                onClick = onClick,
                onTransform = onTransform,
                update = update,
            )
        }
        OPEN_GL -> {
            if (hasGLES20(LocalContext.current)) {
                MapOpenGLViewComposable(
                    modifier = modifier,
                    backgroundColor = backgroundColor,
                    onSizeChanged = onSizeChanged,
                    onClick = onClick,
                    onTransform = onTransform,
                    update = update,
                )
            } else {
                throw IllegalStateException("OpenGL 2.0 not supported")
            }
        }
    }
}