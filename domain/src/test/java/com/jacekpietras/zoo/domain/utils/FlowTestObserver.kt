package com.jacekpietras.zoo.domain.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions.assertEquals

class FlowTestObserver<T>(
    scope: CoroutineScope,
    flow: Flow<T>,
) {
    private val values = mutableListOf<T>()

    private val job: Job = scope.launch {
        flow.collect(values::add)
    }

    fun assertFlowEmpty() {
        assert(values.isEmpty())
    }

    fun assertFlowEquals(vararg values: T) {
        assertEquals(values.toList(), this.values)
    }

    fun finish() {
        job.cancel()
    }
}
