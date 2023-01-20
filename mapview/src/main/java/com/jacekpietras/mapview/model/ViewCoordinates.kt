package com.jacekpietras.mapview.model

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.RectD
import com.jacekpietras.geometry.containsLine
import com.jacekpietras.geometry.haversine
import com.jacekpietras.geometry.polygonContains
import kotlin.math.abs
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

    fun getVisiblePath(array: DoubleArray): List<DoubleArray>? {
        val rectF = visibleRectRotated
        val result = mutableListOf<DoubleArray>()
        var pos = 0
        var skip = 0

        if (part.size < array.size) {
            part = DoubleArray(array.size + 16)
        }

        for (i in 0 until (array.size - 2) step 2) {
            if (skip > 0 || rectF.containsLine(array[i], array[i + 1], array[i + 2], array[i + 3])) {
                if (pos == 0) {
                    part[0] = array[i]
                    part[1] = array[i + 1]
                    part[2] = array[i + 2]
                    part[3] = array[i + 3]
                    pos = 4
                } else {
                    part[pos] = array[i + 2]
                    part[pos + 1] = array[i + 3]
                    pos += 2
                }
                if (skip == 0) {
                    // takes next few segments even if they are not in the screen,
                    // optimization trick to not check if rect contains line
                    skip = 5
                } else {
                    skip--
                }
            } else {
                if (pos != 0) {
                    result.add(part.copyOfRange(0, pos))
                }
                pos = 0
            }
        }

        if (pos != 0) {
            result.add(part.copyOfRange(0, pos))
        }

        return result.takeIf(MutableList<DoubleArray>::isNotEmpty)
    }

    fun isPolygonVisible(array: DoubleArray): Boolean =
        intersectsPolygon(array)

    fun isPointVisible(p: PointD): Boolean =
        visibleRectRotated.contains(p)

    fun transformPath(raw: List<DoubleArray>, translated: List<FloatArray>) {
        raw.mapIndexed { i, double -> transformPolygon(double, translated[i]) }
    }

    fun transformPolygon(input: DoubleArray, output: FloatArray) {
        for (i in input.indices step 2) {
            output[i] = input[i].transformX()
            output[i + 1] = input[i + 1].transformY()
        }
    }

    fun transformPoint(p: PointD, array: FloatArray) {
        array[0] = p.x.transformX()
        array[1] = p.y.transformY()
    }

    fun deTransformPoint(x: Float, y: Float): PointD =
        PointD(
            x = x.deTransformX(),
            y = y.deTransformY(),
        )

    private fun Double.transformX(): Float =
        ((this - visibleRect.left) * horizontalScale).toFloat()

    private fun Double.transformY(): Float =
        ((this - visibleRect.top) * verticalScale).toFloat()

    private fun Float.deTransformX(): Double =
        (this / horizontalScale) + visibleRect.left

    private fun Float.deTransformY(): Double =
        (this / verticalScale) + visibleRect.top

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ViewCoordinates

        if (visibleRectRotated != other.visibleRectRotated) return false

        return true
    }

    override fun hashCode(): Int =
        visibleRectRotated.hashCode()

    fun printDiff(other: ViewCoordinates): Boolean {
        val left = abs(visibleRectRotated.left - other.visibleRectRotated.left) * abs(horizontalScale)
        val top = abs(visibleRectRotated.top - other.visibleRectRotated.top) * abs(verticalScale)
        val right = abs(visibleRectRotated.right - other.visibleRectRotated.right) * abs(horizontalScale)
        val bottom = abs(visibleRectRotated.bottom - other.visibleRectRotated.bottom) * abs(verticalScale)

        return left > COORDINATE_THRESHOLD ||
                top > COORDINATE_THRESHOLD ||
                right > COORDINATE_THRESHOLD ||
                bottom > COORDINATE_THRESHOLD
    }

    private companion object {

        var part = DoubleArray(512)

        const val COORDINATE_THRESHOLD = 500
    }
}