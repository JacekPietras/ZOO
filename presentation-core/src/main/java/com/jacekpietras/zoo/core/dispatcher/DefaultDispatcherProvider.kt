package com.jacekpietras.zoo.core.dispatcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

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
    viewModelScope.launch(dispatcherProvider.default, block = block)

fun ViewModel.launchInIO(block: suspend CoroutineScope.() -> Unit) =
    viewModelScope.launch(dispatcherProvider.io, block = block)

fun ViewModel.launchInMain(block: suspend CoroutineScope.() -> Unit) =
    viewModelScope.launch(dispatcherProvider.main, block = block)

suspend fun <T> ViewModel.onBackground(block: suspend CoroutineScope.() -> T) =
    withContext(dispatcherProvider.default, block)

suspend fun <T> ViewModel.onIO(block: suspend CoroutineScope.() -> T) =
    withContext(dispatcherProvider.io, block)

suspend fun <T> ViewModel.onMain(block: suspend CoroutineScope.() -> T) =
    withContext(dispatcherProvider.main, block)

suspend fun <T> Channel<T>.sendOnMain(element: T) {
    withContext(DispatcherProviderWrapper.provider.main) {
        send(element)
    }
}
