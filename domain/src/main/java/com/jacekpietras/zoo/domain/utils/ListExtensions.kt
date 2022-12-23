package com.jacekpietras.zoo.domain.utils

inline fun <T> List<T>.forEachPair(block: (T, T) -> Unit) {
    val pointsTo = map { it }.toMutableList()
    forEach { from ->
        pointsTo.remove(from)
        pointsTo.forEach { to ->
            block(from, to)
        }
    }
}

inline fun <T> List<T>.forEachPairIndexed(block: (Int, T, Int, T) -> Unit) {
    val pointsTo = mapIndexed { index, it -> index to it }.toMutableList()
    forEachIndexed { fromIndex, from ->
        pointsTo.remove(fromIndex to from)
        pointsTo.forEach { (toIndex, to) ->
            block(fromIndex, from, toIndex, to)
        }
    }
}
