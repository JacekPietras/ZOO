package com.jacekpietras.zoo.planner.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn

internal class MutableStateFlowCombiner<T>(
    flow: Flow<T>,
    scope: CoroutineScope,
    started: SharingStarted,
    initialValue: T,
) : Flow<T?> {

    var value: T?
        set(value) {
            mutableState.value = value
        }
        get() = merged.value

    private val mutableState = MutableStateFlow<T?>(null)
    private val merged = merge(mutableState, flow).stateIn(scope, started, initialValue)

    override suspend fun collect(collector: FlowCollector<T?>) = merged.collect(collector)

    companion object {

        fun <T> Flow<T>.mutableStateIn(
            scope: CoroutineScope,
            started: SharingStarted,
            initialValue: T,
        ) = MutableStateFlowCombiner(
            this,
            scope,
            started,
            initialValue,
        )
    }
}