package com.jacekpietras.mapview.ui

import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.ui.opengl.LinePolygonD

internal interface PathBaker {

    fun bakePathToTriangles(paint: MapPaint, points: List<PointD>): LinePolygonD?
}
