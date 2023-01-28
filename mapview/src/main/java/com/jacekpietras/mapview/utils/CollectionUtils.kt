package com.jacekpietras.mapview.utils

internal fun Collection<Int>.toShortArray(): ShortArray {
    val result = ShortArray(size)
    var index = 0
    for (element in this)
        result[index++] = element.toShort()
    return result
}
