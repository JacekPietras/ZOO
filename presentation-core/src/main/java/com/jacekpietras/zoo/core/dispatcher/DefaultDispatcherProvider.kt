package com.jacekpietras.zoo.core.dispatcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultDispatcherProvider : DispatcherProvider {

    override val main: CoroutineDispatcher = Dispatchers.Main.immediate
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}

interface DispatcherProvider {

    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
    val io: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

object DispatcherProviderWrapper {

    var provider: DispatcherProvider = DefaultDispatcherProvider()
        get() {
            if (field is DefaultDispatcherProvider && isRunningTest) {
                throw IllegalStateException("Not initialized with TestDispatcherProvider")
            }
            return field
        }

    private val isRunningTest: Boolean by lazy(LazyThreadSafetyMode.NONE) {
        try {
            Class.forName("org.junit.jupiter.api.Test")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}

@Suppress("unused")
val ViewModel.dispatcherProvider
    get() = DispatcherProviderWrapper.provider

fun ViewModel.launchInBackground(block: suspend CoroutineScope.() -> Unit) =
    viewModelScope.launch(DispatcherProviderWrapper.provider.default, block = block)

suspend fun <T> onBackground(block: suspend CoroutineScope.() -> T) =
    withContext(DispatcherProviderWrapper.provider.default, block)

suspend fun <T> onMain(block: suspend CoroutineScope.() -> T) =
    withContext(DispatcherProviderWrapper.provider.main, block)

fun <T> Flow<T>.flowOnBackground(): Flow<T> =
    flowOn(DispatcherProviderWrapper.provider.default)
