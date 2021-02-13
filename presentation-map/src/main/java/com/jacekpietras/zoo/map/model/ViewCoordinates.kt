package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.zoo.map.cutOut
import com.jacekpietras.core.containsLine
import com.jacekpietras.core.haversine

internal class ViewCoordinates(
    centerGpsCoordinate: PointD,
    zoom: Double,
    viewWidth: Int,
    viewHeight: Int,
) {

    val visibleRect: RectD
    val horizontalScale: Double
    val verticalScale: Double

    init {
        val haversineH = haversine(
            centerGpsCoordinate.x, centerGpsCoordinate.y + zoom,
            centerGpsCoordinate.x, centerGpsCoordinate.y - zoom,
        )
        val haversineW = haversine(
            centerGpsCoordinate.x - zoom, centerGpsCoordinate.y,
            centerGpsCoordinate.x + zoom, centerGpsCoordinate.y,
        )
        val haversineCorrection = haversineW / haversineH
        val ratioZoom = zoom * (viewHeight / viewWidth.toFloat()) * haversineCorrection

        val visibleGpsCoordinate = RectD(
            centerGpsCoordinate.x - zoom,
            centerGpsCoordinate.y + ratioZoom,
            centerGpsCoordinate.x + zoom,
            centerGpsCoordinate.y - ratioZoom
        )
        visibleRect = visibleGpsCoordinate
        horizontalScale = viewWidth / visibleGpsCoordinate.width()
        verticalScale = viewHeight / visibleGpsCoordinate.height()
    }

    fun transform(path: PathD): List<PathD> =
        path
            .vertices
            .cutOut { a, b -> visibleRect.containsLine(a, b) }
            .map { PathD(it.map { point -> transform(point) }) }

    fun transform(polygon: PolygonD): PolygonD? =
        if (polygon.intersects(visibleRect)) {
            PolygonD(polygon.vertices.map { point -> transform(point) })
        } else {
            null
        }

    fun transform(p: PointD): PointD =
        PointD(
            ((p.x - visibleRect.left) * horizontalScale),
            ((p.y - visibleRect.top) * verticalScale)
        )
}