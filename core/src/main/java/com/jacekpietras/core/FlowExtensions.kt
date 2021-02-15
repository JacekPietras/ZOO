package com.jacekpietras.core

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

fun <T> Flow<T>.catchAndLog(): Flow<T> =
    catch { Timber.e(it) }

inline fun <T> Flow<T>.observe(lifecycleOwner: LifecycleOwner, crossinline block: (T) -> Unit) {
    lifecycleOwner.lifecycleScope.launch {
        catchAndLog().collect { block(it) }
    }
}

inline fun <T> Channel<T>.observe(lifecycleOwner: LifecycleOwner, crossinline block: (T) -> Unit) {
    receiveAsFlow().observe(lifecycleOwner, block)
}
