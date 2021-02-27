package com.jacekpietras.core

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

inline fun <T> Iterable<T>.forEachWithNext(block: (a: T, b: T) -> Unit) {
    val iterator = iterator()
    if (!iterator.hasNext()) return
    var current = iterator.next()
    while (iterator.hasNext()) {
        val next = iterator.next()
        block(current, next)
        current = next
    }
}

inline fun <T, Y, U, R> Collection<Triple<T, Y, U>>.mapTriple(transform: (T, Y, U) -> R): List<R> {
    val destination = ArrayList<R>(this.size)
    forEach { destination.add(transform(it.first, it.second, it.third)) }
    return destination
}

inline fun <T, Y, R> Collection<Pair<T, Y>>.mapPair(transform: (T, Y) -> R): List<R> {
    val destination = ArrayList<R>(this.size)
    forEach { destination.add(transform(it.first, it.second)) }
    return destination
}
