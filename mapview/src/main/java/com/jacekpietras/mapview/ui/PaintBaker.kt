package com.jacekpietras.mapview.ui

import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PaintHolder

interface PaintBaker<T> {

    fun bakeDimension(dimension: MapDimension): (zoom: Double, position: PointD, screenWidthInPixels: Int) -> Float

    fun bakeCanvasPaint(paint: MapPaint): PaintHolder<T>

    fun bakeBorderCanvasPaint(paint: MapPaint): PaintHolder<T>?
}
