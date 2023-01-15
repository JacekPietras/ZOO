package com.jacekpietras.mapview.ui.compose

import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.ui.view.MapCustomView

@Composable
fun ComposableMapCustomView(
    modifier: Modifier = Modifier,
    onSizeChanged: (Int, Int) -> Unit,
    onClick: ((Float, Float) -> Unit)? = null,
    onTransform: ((Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    mapList: List<RenderItem<Paint>>,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapCustomView(context).apply {
                this.onSizeChanged = onSizeChanged
                this.onClick = onClick
                this.onTransform = onTransform
                this.mapList = mapList
            }
        },
    )
}
