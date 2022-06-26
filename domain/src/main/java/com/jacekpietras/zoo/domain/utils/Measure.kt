package com.jacekpietras.zoo.domain.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


@ExperimentalTime
internal suspend fun <Y> measureMap(onMeasure: (Duration) -> Unit, block: suspend () -> Y): Y {
    var result: Y
    val measure = measureTime { result = block() }
    onMeasure(measure)
    return result
}

@ExperimentalTime
internal fun <T, Y> Flow<T>.measureMap(onMeasure: (Duration) -> Unit, block: suspend (T) -> Y): Flow<Y> = map {
    var result: Y
    val measure = measureTime { result = block(it) }
    onMeasure(measure)
    result
}
