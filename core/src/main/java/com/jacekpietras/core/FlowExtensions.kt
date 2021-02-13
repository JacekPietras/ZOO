package com.jacekpietras.core

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import timber.log.Timber

fun <T> Flow<T>.catchAndLog(): Flow<T> =
    catch { Timber.e(it) }

inline fun <T> Fragment.observe(flow: Flow<T>, crossinline block: (T) -> Unit) {
    lifecycleScope.launch {
        flow.catchAndLog().collect { block(it) }
    }
}

inline fun <T> Fragment.observe(channel: Channel<T>, crossinline block: (T) -> Unit) {
    observe(channel.receiveAsFlow(), block)
}