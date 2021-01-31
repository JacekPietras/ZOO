package com.jacekpietras.zoo.map

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.contains
import androidx.core.graphics.toPointF

internal class ViewCoordinates(
    val visibleRect: RectF,
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
                visibleRect = visibleGpsCoordinate,
                horizontalScale = viewWidth / visibleGpsCoordinate.width(),
                verticalScale = viewHeight / visibleGpsCoordinate.height(),
            )
        }
    }

    fun transform(rect: RectF): Rect =
        Rect(
            ((rect.left - visibleRect.left) * horizontalScale).toInt(),
            ((rect.top - visibleRect.top) * verticalScale).toInt(),
            ((rect.right - visibleRect.left) * horizontalScale).toInt(),
            ((rect.bottom - visibleRect.top) * verticalScale).toInt()
        )

    fun transform(p: PointF): Point =
        Point(
            ((p.x - visibleRect.left) * horizontalScale).toInt(),
            ((p.y - visibleRect.top) * verticalScale).toInt()
        )

    fun transform(path: PathF): PathsF =
        PathsF(
            path.list
                .cutOut { a, b -> visibleRect.containsLine(a, b) }
                .map { it.map { point -> transform(point).toPointF() } }
        )

    fun transform(polygon: PolygonF): PolygonF =
        PolygonF(polygon.list.map { point -> transform(point).toPointF() })

    fun intersects(rect: RectF): Boolean =
        visibleRect.intersects(rect.left, rect.top, rect.right, rect.bottom)

    fun intersects(polygon: PolygonF): Boolean =
        polygon.intersects(visibleRect)

    fun contains(p: PointF): Boolean =
        visibleRect.contains(p)
}