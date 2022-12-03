package com.jacekpietras.zoo.domain.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.milliseconds

fun <T> CoroutineScope.assertFlowEquals(flow: Flow<T>, vararg expected: T) {
    val collected = mutableListOf<T>()
    launch(start = CoroutineStart.UNDISPATCHED, context = Dispatchers.Unconfined) {
        flow.collectIndexed { index, item ->
            collected.add(item)
            if (index == expected.size - 1) cancel()
        }
    }
    assertEquals(ArrayList(expected.asList()), ArrayList(collected))
}

@ExperimentalStdlibApi
suspend fun <T> assertFlowEqualsWithTimeout(flow: Flow<T>, vararg expected: T) {
    assertFlowEqualsWithTimeout(flow, timeout = 1000, expected = expected)
}

@ExperimentalStdlibApi
suspend fun <T> assertFlowEqualsWithTimeout(flow: Flow<T>, timeout: Long, vararg expected: T) {
    Dispatchers.setMain(coroutineContext[CoroutineDispatcher.Key] as TestDispatcher)

    val collected = mutableListOf<T>()
    try {
        withTimeout(timeout.milliseconds) {
            flow.collect { item ->
                collected.add(item)
            }
            assertEquals(expected.asList(), collected)
        }
    } catch (ignored: TimeoutCancellationException) {
        assertEquals(expected.asList(), collected)
    }
}

suspend fun <T> CoroutineScope.testFlow(flow: Flow<T>, block: suspend FlowTestObserver<T>.() -> Unit): FlowTestObserver<T> =
    FlowTestObserver(this, flow).apply {
        block()
        finish()
    }
