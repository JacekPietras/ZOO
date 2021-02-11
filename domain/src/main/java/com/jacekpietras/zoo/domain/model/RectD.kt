package com.jacekpietras.zoo.domain.model

import android.graphics.Rect
import android.graphics.RectF

data class RectD(
    val left: Double = 0.0,
    val top: Double = 0.0,
    val right: Double = 0.0,
    val bottom: Double = 0.0,
) {

    fun toShortString(): String = "[$left,$top][$right,$bottom]"
    fun isEmpty(): Boolean = left >= right || top >= bottom
    fun width(): Double = right - left
    fun height(): Double = bottom - top
    fun centerX(): Double = (left + right) * 0.5
    fun centerY(): Double = (top + bottom) * 0.5

    fun contains(x: Double, y: Double): Boolean =
        left < right && top < bottom && x >= left && x < right && y >= top && y < bottom

    fun toFloat(): RectF =
        RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

    fun toInt(): Rect =
        Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
}