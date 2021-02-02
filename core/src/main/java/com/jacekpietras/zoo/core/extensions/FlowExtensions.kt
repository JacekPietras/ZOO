package com.jacekpietras.zoo.core.extensions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber

@ExperimentalCoroutinesApi
fun <T> Flow<T>.catchAndLog(): Flow<T> =
    catch { Timber.e(it) }
