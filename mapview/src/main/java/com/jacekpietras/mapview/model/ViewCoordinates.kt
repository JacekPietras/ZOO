package com.jacekpietras.mapview.model

import com.jacekpietras.core.*
import kotlin.math.pow
import kotlin.math.sqrt

internal class ViewCoordinates(
    centerGpsCoordinate: PointD,
    zoom: Double,
    viewWidth: Int,
    viewHeight: Int,
) {

    val visibleRect: RectD
    private val visibleRectRotated: RectD
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
        val zoomSquare = zoom * (viewHeight / viewWidth.toFloat())
        val ratioZoom = zoomSquare * haversineCorrection

        val circularCorrection = sqrt((ratioZoom / 2).pow(2) + (zoom / 2).pow(2)) / (ratioZoom / 2)

        visibleRect = RectD(
            centerGpsCoordinate.x - zoom,
            centerGpsCoordinate.y + ratioZoom,
            centerGpsCoordinate.x + zoom,
            centerGpsCoordinate.y - ratioZoom,
        )
        visibleRectRotated = RectD(
            centerGpsCoordinate.x - zoomSquare * circularCorrection,
            centerGpsCoordinate.y + ratioZoom * circularCorrection,
            centerGpsCoordinate.x + zoomSquare * circularCorrection,
            centerGpsCoordinate.y - ratioZoom * circularCorrection,
        )

        horizontalScale = viewWidth / visibleRect.width()
        verticalScale = viewHeight / visibleRect.height()
    }

    fun transformPath(array: DoubleArray): List<FloatArray> {
        val rectF = visibleRectRotated
        val result = mutableListOf<FloatArray>()
        var part: FloatArray? = null
        var pos = 0

        for (i in 0 until (array.size - 2) step 2) {
            if (rectF.containsLine(array[i], array[i + 1], array[i + 2], array[i + 3])) {
                if (part != null) {
                    part[pos] = array[i + 2].transformX()
                    part[pos + 1] = array[i + 3].transformY()
                    pos += 2
                } else {
                    part = FloatArray(array.size)

                    part[0] = array[i].transformX()
                    part[1] = array[i + 1].transformY()
                    part[2] = array[i + 2].transformX()
                    part[3] = array[i + 3].transformY()
                    pos = 4
                }
            } else {
                if (part != null) {
                    result.add(part.copyOfRange(0, pos))
                }
                part = null
            }
        }

        if (part != null) {
            result.add(part.copyOfRange(0, pos))
        }

        return result
    }

    fun transformPolygon(array: DoubleArray): FloatArray? =
        if (intersectsPolygon(array)) {
            val result = FloatArray(array.size)

            for (i in array.indices step 2) {
                result[i] = array[i].transformX()
                result[i + 1] = array[i + 1].transformY()
            }
            result
        } else {
            null
        }

    fun transformPoint(p: PointD): FloatArray {
        val result = FloatArray(2)
        result[0] = p.x.transformX()
        result[1] = p.y.transformY()
        return result
    }

    private fun Double.transformX(): Float =
        ((this - visibleRect.left) * horizontalScale).toFloat()

    private fun Double.transformY(): Float =
        ((this - visibleRect.top) * verticalScale).toFloat()

    private fun intersectsPolygon(array: DoubleArray): Boolean {
        val rectF = visibleRectRotated
        if (polygonContains(
                array,
                rectF.left,
                rectF.top
            )
        ) return true

        for (i in 0 until (array.size - 2) step 2) {
            if (rectF.containsLine(
                    array[i],
                    array[i + 1],
                    array[i + 2],
                    array[i + 3]
                )
            ) return true
        }
        return false
    }
}