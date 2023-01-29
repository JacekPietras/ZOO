package com.jacekpietras.mapview.ui

import android.content.Context
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PaintHolder
import com.jacekpietras.mapview.ui.MapRenderConfig.isTriangulated
import com.jacekpietras.mapview.ui.compose.ComposablePaintBaker
import com.jacekpietras.mapview.ui.compose.MapRenderer
import com.jacekpietras.mapview.ui.opengl.OpenGLPaintBaker
import com.jacekpietras.mapview.ui.view.ViewPaintBaker

internal interface PaintBaker<T> {

    fun bakeDimension(dimension: MapDimension): (zoom: Double, position: PointD, screenWidthInPixels: Int) -> Float

    fun bakeCanvasPaint(paint: MapPaint): Pair<PaintHolder<T>, PaintHolder<T>?>

    class Factory {

        @Suppress("UNCHECKED_CAST")
        fun <T> create(context: Context, mapRenderer: MapRenderer, antialiasing: Boolean): PaintBaker<T> =
            when (mapRenderer) {
                MapRenderer.CUSTOM_VIEW,
                MapRenderer.SURFACE_VIEW -> {
                    ViewPaintBaker(context, antialiasing)
                }
                MapRenderer.COMPOSE -> {
                    ComposablePaintBaker(context)
                }
                MapRenderer.OPEN_GL -> {
                    isTriangulated = true
                    OpenGLPaintBaker(context)
                }
            } as PaintBaker<T>
    }
}
