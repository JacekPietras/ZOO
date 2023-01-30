package com.jacekpietras.mapview.ui.opengl

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.inflateLine
import com.jacekpietras.mapview.ui.opengl.LineArrayD.OutlineD
import com.jacekpietras.mapview.ui.opengl.LineArrayD.StripD
import com.jacekpietras.mapview.utils.toDoubleArray

class LinePolygonD(
    val strip: StripD,
    val outline: OutlineD,
) {

    fun copyOfRange(from: Int, to: Int): LinePolygonD =
        LinePolygonD(
            strip = strip.copyOfRange(from, to),
            outline = outline.copyOfRange(from, to),
        )

    companion object {

        fun create(points: List<PointD>, width: Double): LinePolygonD {
            val inflated = inflateLine(points, width)
            val pointsCount = (inflated.size shr 1) - 1
            val strip = DoubleArray(inflated.size shl 1)
            for (i in 0..pointsCount) {
                strip[i shl 2] = inflated[i].x
                strip[(i shl 2) + 1] = inflated[i].y
                strip[(i shl 2) + 2] = inflated[inflated.lastIndex - i].x
                strip[(i shl 2) + 3] = inflated[inflated.lastIndex - i].y
            }
            val outline = inflated.toDoubleArray()

            return LinePolygonD(
                strip = StripD(strip),
                outline = OutlineD(outline),
            )
        }
    }
}

sealed class LineArrayD(
    open val array: DoubleArray,
) {

    class StripD(
        override val array: DoubleArray,
    ) : LineArrayD(array) {

        fun copyOfRange(from: Int, to: Int): StripD =
            StripD(array.copyOfRange(from shl 1, to shl 1))
    }

    class OutlineD(
        override val array: DoubleArray,
    ) : LineArrayD(array) {

        fun copyOfRange(from: Int, to: Int): OutlineD {
            val size = to - from
            val result = DoubleArray(size shl 1)
            (0 until size).forEach { i ->
                result[i] = array[i + from]
                result[i + size] = array[array.lastIndex - (to - 1) + i]
            }
            return OutlineD(result)
        }
    }
}