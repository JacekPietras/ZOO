package com.jacekpietras.zoo.domain.model

import android.graphics.Point
import android.graphics.PointF

data class PointD(
    var x: Double = 0.0,
    var y: Double = 0.0,
) {

    constructor(xFloat: Float, yFloat: Float) : this(xFloat.toDouble(), yFloat.toDouble())

    constructor(pair:Pair<Number, Number>) : this(pair.first.toDouble(), pair.second.toDouble())

    fun toFloat(): PointF =
        PointF(x.toFloat(), y.toFloat())

    fun toInt(): Point =
        Point(x.toInt(), y.toInt())

    operator fun minusAssign(right: PointD) {
        x -= right.x
        y -= right.y
    }

    operator fun plusAssign(right: PointD) {
        x += right.x
        y += right.y
    }
}
