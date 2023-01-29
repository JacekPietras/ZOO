package com.jacekpietras.mapview.utils

import com.jacekpietras.geometry.PointD

internal fun Collection<Int>.toShortArray(): ShortArray {
    val result = ShortArray(size)
    var index = 0
    for (element in this)
        result[index++] = element.toShort()
    return result
}

fun pointsToDoubleArray(list: List<PointD>): DoubleArray {
    val result = DoubleArray(list.size * 2)
    for (i in list.indices) {
        result[i shl 1] = list[i].x
        result[(i shl 1) + 1] = list[i].y
    }
    return result
}

fun List<PointD>.toDoubleArray(): DoubleArray =
    pointsToDoubleArray(this)