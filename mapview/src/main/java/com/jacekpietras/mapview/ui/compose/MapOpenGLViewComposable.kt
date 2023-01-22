package com.jacekpietras.mapview.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.jacekpietras.mapview.model.OpenGLPaint
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.ui.opengl.MapOpenGLView

@Composable
fun MapOpenGLViewComposable(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onSizeChanged: (Int, Int) -> Unit,
    onClick: ((Float, Float) -> Unit)? = null,
    onTransform: ((Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    update: ((List<RenderItem<Any>>) -> Unit) -> Unit,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapOpenGLView(context).apply {
                update.invoke {
                    @Suppress("UNCHECKED_CAST")
                    mapList = it as List<RenderItem<OpenGLPaint>>
                }
                this.onSizeChanged = onSizeChanged
                this.onClick = onClick
                this.onTransform = onTransform
                this.openGLBackground = backgroundColor.toArgb()
            }
        },
    )
}
