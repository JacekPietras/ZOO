package com.jacekpietras.zoo.core.extensions

import android.annotation.SuppressLint
import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.jacekpietras.zoo.core.dispatcher.DispatcherProviderWrapper
import com.jacekpietras.zoo.core.dispatcher.dispatcherProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

suspend inline fun <T> MutableLiveData<T>.reduceOnMain(crossinline block: suspend T.() -> T) {
    withContext(DispatcherProviderWrapper.provider.main) {
        value = block(checkNotNull(value))
    }
}

suspend inline fun <T> NullSafeMutableLiveData<T>.reduceOnMain(crossinline block: suspend T.() -> T) {
    withContext(DispatcherProviderWrapper.provider.main) {
        value = block(value)
    }
}

suspend fun <T> ViewModel.onMain(block: suspend CoroutineScope.() -> T) {
    withContext(dispatcherProvider.main, block)
}

class NullSafeMutableLiveData<T>(value: T) : MutableLiveData<T>(value) {

    override fun getValue(): T = checkNotNull(super.getValue())
}

@PublishedApi
internal class ObserverImpl<T>(
    lifecycleOwner: LifecycleOwner,
    private val flow: Flow<T>,
    private val collector: suspend (T) -> Unit
) : DefaultLifecycleObserver {

    private var job: Job? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        job = owner.lifecycleScope.launch {
            flow.collect {
                collector(it)
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        job?.cancel()
        job = null
    }
}

// todo check if that works better https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
fun <T> Flow<T>.observe(
    lifecycleOwner: LifecycleOwner,
    collector: suspend (T) -> Unit
) {
    ObserverImpl(lifecycleOwner, this, collector)
}

@SuppressLint("NullSafeMutableLiveData")
@MainThread
fun <X, Y> LiveData<X>.mapInBackground(mapFunction: (X) -> Y): LiveData<Y> {
    val result = MediatorLiveData<Y>()
    val scope = CoroutineScope(DispatcherProviderWrapper.provider.default)
    var job: Job? = null

    result.addSource(this, Observer<X> { x ->
        if (x == null) return@Observer
        job?.cancel()
        job = scope.launch {
            val mapped = mapFunction(x)
            val parent = this
            ensureActive()
            withContext(DispatcherProviderWrapper.provider.main) {
                parent.ensureActive()
                result.value = mapped
            }
            job = null
        }
    })

    return result
}

@MainThread
fun <X, Y> LiveData<X>.mapNotNullInBackground(mapFunction: (X) -> Y?): LiveData<Y> {
    val result = MediatorLiveData<Y>()
    val scope = CoroutineScope(DispatcherProviderWrapper.provider.default)
    var job: Job? = null

    result.addSource(this, Observer<X> { x ->
        if (x == null) return@Observer
        job?.cancel()
        job = scope.launch {
            val mapped = mapFunction(x)
            val parent = this
            ensureActive()
            withContext(DispatcherProviderWrapper.provider.main) {
                parent.ensureActive()
                mapped?.let { result.value = it }
            }
            job = null
        }
    })

    return result
}
