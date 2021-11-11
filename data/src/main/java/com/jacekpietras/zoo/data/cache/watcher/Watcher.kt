package com.jacekpietras.zoo.data.cache.watcher

import kotlinx.coroutines.flow.Flow

internal interface Watcher<T> {

    val lastValue: T
    val dataFlow: Flow<T>

    fun notifyUpdated(data: T)
}
