package com.jacekpietras.mapview.ui.compose

import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.ui.LastMapUpdate
import com.jacekpietras.mapview.ui.LastMapUpdate.cutoE
import com.jacekpietras.mapview.ui.LastMapUpdate.cutoS
import com.jacekpietras.mapview.ui.LastMapUpdate.mergE
import com.jacekpietras.mapview.ui.LastMapUpdate.moveE
import com.jacekpietras.mapview.ui.LastMapUpdate.rendE
import com.jacekpietras.mapview.ui.LastMapUpdate.rendS
import com.jacekpietras.mapview.ui.LastMapUpdate.sortE
import com.jacekpietras.mapview.ui.LastMapUpdate.sortS
import com.jacekpietras.mapview.ui.LastMapUpdate.trans
import com.jacekpietras.mapview.ui.view.MapSurfaceView
import timber.log.Timber

@Composable
fun MapSurfaceViewComposable(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onSizeChanged: (Int, Int) -> Unit,
    onClick: ((Float, Float) -> Unit)? = null,
    onTransform: ((Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    update: ((List<RenderItem<Paint>>) -> Unit) -> Unit,
) {
    var mapView by remember { mutableStateOf<MapSurfaceView?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapSurfaceView(context).apply {
                update.invoke { this.mapList = it }
                this.onSizeChanged = onSizeChanged
                this.onClick = onClick
                this.onTransform = onTransform
                this.setBackgroundColor(backgroundColor.toArgb())
                mapView = this
            }
        },
    )

    val prevRendE = rendE
    rendE = System.nanoTime()
    if (trans > 0) {

        Timber.d(
            "Perf: Render: Full: ${trans toMs rendE}, from prev ${prevRendE toMs rendE}\n" +
                    "    [pass to vm] ${trans toMs cutoS}\n" +
                    "    [coord prep] ${cutoS toMs moveE}\n" +
                    "    [ translate] ${moveE toMs sortS}\n" +
                    "    [      sort] ${sortS toMs sortE}\n" +
                    "    [       sum] ${sortE toMs mergE}\n" +
                    "    [invali req] ${mergE toMs cutoE}\n" +
                    "    [invalidate] ${cutoE toMs rendS}\n" +
                    "    [    render] ${rendS toMs rendE}"
        )
    }
}

private infix fun Long.toMs(right: Long) =
    "${(right - this) / 10_000 / 1_00.0} ms"
