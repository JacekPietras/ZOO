package com.jacekpietras.zoo.domain.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions.assertEquals

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