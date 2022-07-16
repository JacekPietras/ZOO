package com.jacekpietras.zoo.data.cache.watcher

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

internal inline fun <reified T> buildStateFlow(initValue: T? = null): Watcher<T> =
    if (null is T) {
        buildNullableStateFlow(initValue)
    } else {
        buildNullSafeStateFlow(initValue)
    }

@Suppress("UNCHECKED_CAST")
internal fun <T> buildNullableStateFlow(initValue: T? = null): Watcher<T> =
    NullableColdWatcher(initValue) as Watcher<T>

@Suppress("UNCHECKED_CAST", "USELESS_CAST")
internal fun <T> buildNullSafeStateFlow(initValue: T? = null): Watcher<T> =
    NullSafeColdWatcher(initValue?.let { it as Any }) as Watcher<T>

private class NullableColdWatcher<T>(initValue: T? = null) : Watcher<T?> {

    private val _mutableFlow = MutableStateFlow(initValue)
    override val dataFlow: Flow<T?> = _mutableFlow
    override val lastValue: T? get() = _mutableFlow.value

    override fun notifyUpdated(data: T?) {
        _mutableFlow.value = data
    }
}

private class NullSafeColdWatcher<T : Any>(initValue: T? = null) : Watcher<T> {

    private val _mutableFlow = MutableStateFlow(initValue)
    override val dataFlow: Flow<T> = _mutableFlow.filterNotNull()
    override val lastValue: T get() = checkNotNull(_mutableFlow.value)

    override fun notifyUpdated(data: T) {
        _mutableFlow.value = data
    }
}
