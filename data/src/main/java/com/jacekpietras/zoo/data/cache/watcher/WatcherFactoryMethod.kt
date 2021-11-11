package com.jacekpietras.zoo.data.cache.watcher

import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart

internal inline fun <reified T> buildHotWatcher(initValue: T? = null, buffer: Int = 1): Watcher<T> =
    if (null is T) {
        buildNullableHotWatcher(initValue, buffer)
    } else {
        buildNullSafeHotWatcher(initValue, buffer)
    }

internal inline fun <reified T> buildColdWatcher(initValue: T? = null): Watcher<T> =
    if (null is T) {
        buildNullableColdWatcher(initValue)
    } else {
        buildNullSafeColdWatcher(initValue)
    }

@Suppress("UNCHECKED_CAST")
internal fun <T> buildNullableColdWatcher(initValue: T? = null): Watcher<T> =
    NullableColdWatcher(initValue) as Watcher<T>

@Suppress("UNCHECKED_CAST", "USELESS_CAST")
internal fun <T> buildNullSafeColdWatcher(initValue: T? = null): Watcher<T> =
    NullSafeColdWatcher(initValue?.let { it as Any }) as Watcher<T>

@Suppress("UNCHECKED_CAST")
internal fun <T> buildNullableHotWatcher(initValue: T? = null, buffer: Int): Watcher<T> =
    NullableHotWatcher(initValue, buffer) as Watcher<T>

@Suppress("UNCHECKED_CAST", "USELESS_CAST")
internal fun <T> buildNullSafeHotWatcher(initValue: T? = null, buffer: Int): Watcher<T> =
    NullSafeHotWatcher(initValue?.let { it as Any }, buffer) as Watcher<T>

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

private class NullableHotWatcher<T>(initValue: T? = null, buffer: Int) : Watcher<T?> {

    private val _mutableFlow = MutableSharedFlow<T?>(0, buffer, DROP_OLDEST)
    override val dataFlow: Flow<T?> = _mutableFlow
        .asSharedFlow()
        .onStart { initValue?.let { emit(it) } }
    override val lastValue: T? get() = cachedValue
    private var cachedValue: T? = null

    override fun notifyUpdated(data: T?) {
        cachedValue = data
        _mutableFlow.tryEmit(data)
    }
}

private class NullSafeHotWatcher<T : Any>(initValue: T? = null, buffer: Int) : Watcher<T> {

    private val _mutableFlow = MutableSharedFlow<T>(0, buffer, DROP_OLDEST)
    override val dataFlow: Flow<T> = _mutableFlow
        .asSharedFlow()
        .onStart { initValue?.let { emit(it) } }
        .filterNotNull()
    override val lastValue: T get() = checkNotNull(cachedValue)
    private var cachedValue: T? = null

    override fun notifyUpdated(data: T) {
        cachedValue = data
        _mutableFlow.tryEmit(data)
    }
}
