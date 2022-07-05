package com.jacekpietras.zoo.core.extensions

import android.annotation.SuppressLint
import androidx.annotation.MainThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.jacekpietras.zoo.core.dispatcher.DispatcherProviderWrapper
import com.jacekpietras.zoo.core.dispatcher.dispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend inline fun <T> MutableLiveData<T>.reduceOnMain(crossinline block: suspend T.() -> T) {
    withContext(DispatcherProviderWrapper.provider.main) {
        value = block(checkNotNull(value))
    }
}

suspend inline fun <T> MutableStateFlow<T>.reduceOnMain(crossinline block: suspend T.() -> T) {
    withContext(DispatcherProviderWrapper.provider.main) {
        value = block(value)
    }
}

suspend fun <T> ViewModel.onMain(block: suspend CoroutineScope.() -> T) {
    withContext(dispatcherProvider.main, block)
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
