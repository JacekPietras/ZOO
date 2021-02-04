package com.jacekpietras.zoo.core.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

fun <T> Flow<T>.catchAndLog(): Flow<T> =
    catch { Timber.e(it) }

fun <T> Fragment.observe(flow: Flow<T>, block: (T) -> Unit) {
    lifecycleScope.launch {
        flow.catchAndLog().collect { block(it) }
    }
}