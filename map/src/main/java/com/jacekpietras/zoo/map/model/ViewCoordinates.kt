package com.jacekpietras.zoo.map.model

import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import androidx.core.graphics.toPointF
import com.jacekpietras.zoo.map.cutOut
import com.jacekpietras.zoo.map.utils.containsLine

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

    fun transform(path: PathF): List<PathF> =
        path
            .vertices
            .cutOut { a, b -> visibleRect.containsLine(a, b) }
            .map { PathF(it.map { point -> transform(point).toPointF() }) }

    fun transform(polygon: PolygonF): PolygonF? =
        if (polygon.intersects(visibleRect)) {
            PolygonF(polygon.vertices.map { point -> transform(point).toPointF() })
        } else {
            null
        }

    private fun transform(p: PointF): Point =
        Point(
            ((p.x - visibleRect.left) * horizontalScale).toInt(),
            ((p.y - visibleRect.top) * verticalScale).toInt()
        )
}