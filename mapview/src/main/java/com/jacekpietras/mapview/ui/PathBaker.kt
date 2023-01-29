package com.jacekpietras.mapview.ui

import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.MapPaint

internal interface PathBaker {

    fun bakePath(paint: MapPaint, points: List<PointD>): Pair<DoubleArray?, DoubleArray?>?
}
