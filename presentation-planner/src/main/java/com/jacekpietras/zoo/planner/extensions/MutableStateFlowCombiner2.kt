package com.jacekpietras.zoo.planner.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

internal class MutableStateFlowCombiner2<T : Any>(
    private val flow: Flow<T>,
) : Flow<T?> {

    var value: T?
        set(value) {
            mutableState.value = value
        }
        get() = mutableState.value

    private val mutableState = MutableStateFlow<T?>(null)

    override suspend fun collect(collector: FlowCollector<T?>) = mutableState
        .combineWithIgnoredFlow(flow.onEach { mutableState.value = it })
        .collect(collector)

    companion object {

        fun <T : Any> Flow<T>.mutableStateIn2(
        ) = MutableStateFlowCombiner2(
            this,
        )
    }
}