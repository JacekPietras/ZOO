package com.jacekpietras.mapview.model

import com.jacekpietras.core.*
import kotlin.math.max

internal class ViewCoordinates(
    centerGpsCoordinate: PointD,
    zoom: Double,
    viewWidth: Int,
    viewHeight: Int,
    worldRotation: Float,
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

        if (worldRotation != 0f) {
            val bigger1:Double = max(zoom, ratioZoom)
            val bigger2 = max(viewWidth, viewHeight)
            val visibleGpsCoordinate =  RectD(
                centerGpsCoordinate.x - bigger1,
                centerGpsCoordinate.y + bigger1,
                centerGpsCoordinate.x + bigger1,
                centerGpsCoordinate.y - bigger1,
            )
            visibleRect = visibleGpsCoordinate
            horizontalScale = viewWidth / visibleGpsCoordinate.width()
            verticalScale = viewHeight / visibleGpsCoordinate.height()

        } else {
            val visibleGpsCoordinate =  RectD(
                centerGpsCoordinate.x - zoom,
                centerGpsCoordinate.y + ratioZoom,
                centerGpsCoordinate.x + zoom,
                centerGpsCoordinate.y - ratioZoom,
            )
            visibleRect = visibleGpsCoordinate
            horizontalScale = viewWidth / visibleGpsCoordinate.width()
            verticalScale = viewHeight / visibleGpsCoordinate.height()
        }
    }

    fun transformPath(array: FloatArray): List<FloatArray> {
        val rectF = visibleRect.toFloat()
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

    fun transformPath(array: DoubleArray): List<DoubleArray> {
        val rectF = visibleRect
        val result = mutableListOf<DoubleArray>()
        var part: DoubleArray? = null
        var pos = 0

        for (i in 0 until (array.size - 2) step 2) {
            if (rectF.containsLine(array[i], array[i + 1], array[i + 2], array[i + 3])) {
                if (part != null) {
                    part[pos] = array[i + 2].transformX()
                    part[pos + 1] = array[i + 3].transformY()
                    pos += 2
                } else {
                    part = DoubleArray(array.size)

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

    fun transformPolygon(array: FloatArray): FloatArray? =
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

    fun transformPolygon(array: DoubleArray): DoubleArray? =
        if (intersectsPolygon(array)) {
            val result = DoubleArray(array.size)

            for (i in array.indices step 2) {
                result[i] = array[i].transformX()
                result[i + 1] = array[i + 1].transformY()
            }
            result
        } else {
            null
        }

    fun transformPoint(p: PointD): PointD =
        PointD(p.x.transformX(), p.y.transformY())

    private fun Double.transformX(): Double =
        ((this - visibleRect.left) * horizontalScale)

    private fun Double.transformY(): Double =
        ((this - visibleRect.top) * verticalScale)

    private fun Float.transformX(): Float =
        ((this - visibleRect.left) * horizontalScale).toFloat()

    private fun Float.transformY(): Float =
        ((this - visibleRect.top) * verticalScale).toFloat()

    private fun intersectsPolygon(array: FloatArray): Boolean {
        if (polygonContains(
                array,
                visibleRect.left.toFloat(),
                visibleRect.top.toFloat()
            )
        ) return true
        val rectF = visibleRect.toFloat()

        for (i in 0 until (array.size - 2) step 2) {
            if (rectF.containsLine(array[i], array[i + 1], array[i + 2], array[i + 3])) return true
        }
        return false
    }

    private fun intersectsPolygon(array: DoubleArray): Boolean {
        if (polygonContains(
                array,
                visibleRect.left,
                visibleRect.top
            )
        ) return true

        for (i in 0 until (array.size - 2) step 2) {
            if (visibleRect.containsLine(
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