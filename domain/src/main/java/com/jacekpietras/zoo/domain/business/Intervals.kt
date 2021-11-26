package com.jacekpietras.zoo.domain.business

class Intervals<T : Comparable<T>>(
    range: List<ClosedRange<T>>
) {

    constructor(vararg range: ClosedRange<T>) : this(range.toList())

    private var _ranges: List<Interval<T>> = range.map { it.toInterval() }

    fun toDoubleArray(): DoubleArray {
        val result = DoubleArray(_ranges.size * 2)
        for (i in _ranges.indices) {
            result[i shl 1] = (_ranges[i].start as Number).toDouble()
            result[(i shl 1) + 1] = (_ranges[i].end as Number).toDouble()
        }
        return result
    }

    operator fun plus(range: ClosedRange<T>): Intervals<T> =
        plus(range.toInterval())

    operator fun minus(range: ClosedRange<T>): Intervals<T> =
        minus(range.toInterval())

    private operator fun plus(range: Interval<T>): Intervals<T> {
        val result = mutableListOf<Interval<T>>()
        var start: T? = null
        var end: T? = null
        var added = false

        _ranges.forEach {
            when {
                it.end < range.startMinusAccuracy -> {
                    result.add(it)
                }
                it.startMinusAccuracy > range.end -> {
                    if (start != null && end != null) {
                        result.add(Interval(start!!, end!!))
                        start = null
                        end = null
                        added = true
                    }
                    if (!added) {
                        result.add(range)
                        added = true
                    }
                    result.add(it)
                }
                else -> {
                    start = min(start, range.start, it.start)
                    end = max(end, range.end, it.end)
                }
            }
        }
        if (start != null && end != null) {
            result.add(Interval(start!!, end!!))
            added = true
        }
        if (!added) {
            result.add(range)
        }
        return Intervals<T>()
            .also { it._ranges = result }
    }

    private operator fun minus(range: Interval<T>): Intervals<T> {
        val result = mutableListOf<Interval<T>>()
        _ranges
            .forEach {
                when {
                    range.contains(it) -> {}
                    it.contains(range) -> {
                        val first = it.start..range.start
                        val second = range.end..it.end
                        if (!first.isEmpty()) result.add(first.toInterval())
                        if (!second.isEmpty()) result.add(second.toInterval())
                    }
                    it.overlaps(range) -> {
                        if (it.start < range.start) {
                            result.add(Interval(it.start, range.start))
                        } else {
                            result.add(Interval(range.end, it.end))
                        }
                    }
                    else -> result.add(it)
                }
            }
        return Intervals<T>()
            .also { intervals -> intervals._ranges = result.filter { it.start < it.end } }
    }

    operator fun plus(range: Intervals<T>): Intervals<T> {
        var result = this
        range._ranges.forEach {
            result += it
        }
        return result
    }

    operator fun minus(range: Intervals<T>): Intervals<T> {
        var result = this
        range._ranges.forEach {
            result -= it
        }
        return result
    }

    private fun min(first: T?, second: T?): T? {
        if (first == null) return second
        if (second == null) return first
        return if (first < second) {
            first
        } else {
            second
        }
    }

    private fun min(first: T?, second: T?, third: T?): T? {
        if (first == null) return min(second, third)
        if (second == null) return min(first, third)
        if (third == null) return min(first, second)
        return min(first, min(second, third))
    }

    private fun max(first: T?, second: T?): T? {
        if (first == null) return second
        if (second == null) return first
        return if (first > second) {
            first
        } else {
            second
        }
    }

    private fun max(first: T?, second: T?, third: T?): T? {
        if (first == null) return max(second, third)
        if (second == null) return max(first, third)
        if (third == null) return max(first, second)
        return max(first, max(second, third))
    }

    private fun Interval<T>.contains(right: Interval<T>): Boolean =
        this.start < right.start && this.end > right.end

    private fun Interval<T>.overlaps(right: Interval<T>): Boolean =
        this.start <= right.end && right.start <= this.end

    private fun ClosedRange<T>.toInterval() = Interval(start, endInclusive)

    override fun toString(): String = _ranges.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is ClosedRange<*>) {
            if (_ranges.size != 1) return false
            return _ranges.first().start == other.start && _ranges.first().end == other.endInclusive
        }
        if (javaClass != other?.javaClass) return false

        other as Intervals<*>

        if (_ranges != other._ranges) return false

        return true
    }

    override fun hashCode(): Int {
        return _ranges.map { it.start to it.end }.hashCode()
    }

    private data class Interval<T : Comparable<T>>(
        val start: T,
        val end: T,
    ) {

        @Suppress("UNCHECKED_CAST")
        val startMinusAccuracy: T =
            if (start is Double) {
                (start - ACCURACY) as T
            } else {
                start
            }
    }

    private companion object {

        const val ACCURACY = 0.000001
    }
}

internal operator fun <T : Comparable<T>> ClosedRange<T>.plus(range: ClosedRange<T>): Intervals<T> =
    Intervals(this) + range

internal operator fun <T : Comparable<T>> ClosedRange<T>.minus(range: ClosedRange<T>): Intervals<T> =
    Intervals(this) - range
