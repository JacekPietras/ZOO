package com.jacekpietras.core

import android.graphics.Point
import android.graphics.PointF

data class PointD(
    var x: Double = 0.0,
    var y: Double = 0.0,
) {

    constructor(xFloat: Float, yFloat: Float) : this(xFloat.toDouble(), yFloat.toDouble())

    constructor(pair: Pair<Number, Number>) : this(pair.first.toDouble(), pair.second.toDouble())

    fun toFloat(): PointF =
        PointF(x.toFloat(), y.toFloat())

    fun toInt(): Point =
        Point(x.toInt(), y.toInt())

    operator fun timesAssign(times: Double) {
        x *= times
        y *= times
    }

    operator fun timesAssign(times: Float) {
        x *= times
        y *= times
    }

    operator fun times(times: Double): PointD =
        PointD(
            x * times,
            y * times
        )

    operator fun times(times: Float): PointD =
        PointD(
            x * times,
            y * times
        )

    operator fun plus(second: PointD): PointD =
        PointD(
            x + second.x,
            y + second.y
        )

    operator fun minus(second: PointD): PointD =
        PointD(
            x - second.x,
            y - second.y
        )

    fun toShortString(): String = "[${x.form()},${y.form()}]"

    private fun Double.form() = "%.6f".format(this)
}
