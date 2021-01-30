package com.jacekpietras.zoo.map

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.contains
import androidx.core.graphics.toPointF

internal class ViewCoordinates(
    private val visibleGpsCoordinate: RectF,
    val horizontalScale: Float,
    val verticalScale: Float,
) {

    companion object {

        fun create(
            centerGpsCoordinate: PointF,
            zoom: Float,
            viewWidth: Int,
            viewHeight: Int
        ): ViewCoordinates {
            val ratioZoom = zoom * (viewHeight / viewWidth.toFloat())
            val visibleGpsCoordinate = RectF(
                centerGpsCoordinate.x - zoom,
                centerGpsCoordinate.y - ratioZoom,
                centerGpsCoordinate.x + zoom,
                centerGpsCoordinate.y + ratioZoom
            )
            return ViewCoordinates(
                visibleGpsCoordinate = visibleGpsCoordinate,
                horizontalScale = viewWidth / visibleGpsCoordinate.width(),
                verticalScale = viewHeight / visibleGpsCoordinate.height(),
            )
        }
    }

    fun transform(rect: RectF): Rect =
        Rect(
            ((rect.left - visibleGpsCoordinate.left) * horizontalScale).toInt(),
            ((rect.top - visibleGpsCoordinate.top) * verticalScale).toInt(),
            ((rect.right - visibleGpsCoordinate.left) * horizontalScale).toInt(),
            ((rect.bottom - visibleGpsCoordinate.top) * verticalScale).toInt()
        )

    fun transform(p: PointF): Point =
        Point(
            ((p.x - visibleGpsCoordinate.left) * horizontalScale).toInt(),
            ((p.y - visibleGpsCoordinate.top) * verticalScale).toInt()
        )

    fun transform(path: PathF): PathsF =
        PathsF(
            path.list
                .cutOut { a, b -> visibleGpsCoordinate.containsLine(a, b) }
                .map { it.map { point -> transform(point).toPointF() } }
        )

    fun transform(path: DashedPathF): DashedPathsF =
        DashedPathsF(
            path.list
                .cutOut { a, b -> visibleGpsCoordinate.containsLine(a, b) }
                .map { it.map { point -> transform(point).toPointF() } }
        )

    fun transform(polygon: PolygonF): PolygonF =
        PolygonF(polygon.list.map { point -> transform(point).toPointF() })

    fun intersects(rect: RectF): Boolean =
        visibleGpsCoordinate.intersects(rect.left, rect.top, rect.right, rect.bottom)

    fun intersects(polygon: PolygonF): Boolean =
        polygon.intersects(visibleGpsCoordinate)

    fun contains(p: PointF): Boolean =
        visibleGpsCoordinate.contains(p)
}