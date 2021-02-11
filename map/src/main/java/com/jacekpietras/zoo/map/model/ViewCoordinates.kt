package com.jacekpietras.zoo.map.model

import com.jacekpietras.zoo.domain.model.PointD
import com.jacekpietras.zoo.domain.model.RectD
import com.jacekpietras.zoo.map.cutOut
import com.jacekpietras.zoo.map.utils.containsLine

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
            val ratioZoom = zoom * (viewHeight / viewWidth.toFloat())
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