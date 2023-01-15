package com.jacekpietras.mapview.ui.compose

import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.ui.view.MapCustomView

@Composable
fun MapCustomViewComposable(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onSizeChanged: (Int, Int) -> Unit,
    onClick: ((Float, Float) -> Unit)? = null,
    onTransform: ((Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    mapList: List<RenderItem<Paint>>,
) {
    var mapView by remember { mutableStateOf<MapCustomView?>(null) }
    mapView?.mapList = mapList

    AndroidView(
        modifier = modifier.background(backgroundColor),
        factory = { context ->
            MapCustomView(context).apply {
                this.onSizeChanged = onSizeChanged
                this.onClick = onClick
                this.onTransform = onTransform
                this.mapList = mapList
                mapView = this
            }
        },
    )
}
