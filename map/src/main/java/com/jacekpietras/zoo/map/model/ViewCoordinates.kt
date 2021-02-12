package com.jacekpietras.zoo.map.model

import com.jacekpietras.zoo.domain.model.PointD
import com.jacekpietras.zoo.domain.model.RectD
import com.jacekpietras.zoo.map.cutOut
import com.jacekpietras.zoo.map.utils.containsLine
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal class ViewCoordinates(
    val visibleRect: RectD,
    val horizontalScale: Double,
    val verticalScale: Double,
) {

    companion object {

        fun create(
            centerGpsCoordinate: PointD,
            zoom: Double,
            viewWidth: Int,
            viewHeight: Int
        ): ViewCoordinates {
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
            return ViewCoordinates(
                visibleRect = visibleGpsCoordinate,
                horizontalScale = viewWidth / visibleGpsCoordinate.width(),
                verticalScale = viewHeight / visibleGpsCoordinate.height(),
            )
        }

        private fun haversine(p1: PointD, p2: PointD): Double {
            val dLat: Double = Math.toRadians(p2.y - p1.y)
            val dLon: Double = Math.toRadians(p2.x - p1.x)
            val lat1R = Math.toRadians(p1.y)
            val lat2R = Math.toRadians(p2.y)

            val a = sin(dLat * 0.5).pow2() + sin(dLon * 0.5).pow2() * cos(lat1R) * cos(lat2R)
            return 12745.6 * asin(sqrt(a))
        }

        private fun haversine(p1x: Double, p1y: Double, p2x: Double, p2y: Double): Double {
            val dLat: Double = Math.toRadians(p2y - p1y)
            val dLon: Double = Math.toRadians(p2x - p1x)
            val lat1R = Math.toRadians(p1y)
            val lat2R = Math.toRadians(p2y)

            val a = sin(dLat * 0.5).pow2() + sin(dLon * 0.5).pow2() * cos(lat1R) * cos(lat2R)
            return 12745.6 * asin(sqrt(a))
        }

        private fun Double.pow2(): Double = this * this
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