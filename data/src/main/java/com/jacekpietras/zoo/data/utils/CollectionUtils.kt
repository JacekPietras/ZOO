package com.jacekpietras.zoo.data.utils

inline fun <T> Iterable<T>.cutOut(
    predicate: (a: T, b: T) -> Boolean,
): List<List<T>> {
    val iterator = iterator()
    if (!iterator.hasNext()) return emptyList()

    val result = mutableListOf<List<T>>()
    var part: MutableList<T>? = null
    var current = iterator.next()
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (predicate(current, next)) {
            if (part == null) {
                part = mutableListOf(current, next)
            } else {
                part.add(next)
            }
        } else {
            if (part?.isNotEmpty() == true) {
                result.add(part)
            }
            part = null
        }
        current = next
    }
    if (part?.isNotEmpty() == true) {
        result.add(part)
    }
    return result
}
