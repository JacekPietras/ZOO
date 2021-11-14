package com.jacekpietras.core

import android.graphics.Rect
import android.graphics.RectF

data class RectD(
    val left: Double = 0.0,
    val top: Double = 0.0,
    val right: Double = 0.0,
    val bottom: Double = 0.0,
) {

    fun toShortString(): String = "[${left.form()},${top.form()}][${right.form()},${bottom.form()}]"
    fun isEmpty(): Boolean = left >= right || top >= bottom
    fun notInitialized(): Boolean = width() == 0.0 || height() == 0.0
    fun isNotEmpty(): Boolean = !isEmpty()
    fun width(): Double = right - left
    fun height(): Double = bottom - top
    fun centerX(): Double = (left + right) * 0.5
    fun centerY(): Double = (top + bottom) * 0.5

    fun contains(x: Double, y: Double): Boolean =
        if (top < bottom) {
            if (left < right) {
                x >= left && x < right && y >= top && y < bottom
            } else {
                x >= right && x < left && y >= top && y < bottom
            }
        } else {
            if (left < right) {
                x >= left && x < right && y >= bottom && y < top
            } else {
                x >= right && x < left && y >= bottom && y < top
            }
        }

    fun contains(point: PointD): Boolean = contains(point.x, point.y)

    fun toFloat(): RectF =
        RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

    fun toInt(): Rect =
        Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

    fun toPoints(): List<PointD> =
        listOf(
            PointD(left, top),
            PointD(right, top),
            PointD(right, bottom),
            PointD(left, bottom),
        )

    private fun Double.form() = "%.6f".format(this)
}
