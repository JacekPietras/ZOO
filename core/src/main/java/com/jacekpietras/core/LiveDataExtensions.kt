package com.jacekpietras.core


import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

inline fun <T> MutableLiveData<T>.reduce(block: T.() -> T) {
    value = block(checkNotNull(value))
}

inline fun <T> NullSafeMutableLiveData<T>.reduce(block: T.() -> T) {
    value = block(value)
}

class NullSafeMutableLiveData<T>(value: T) : MutableLiveData<T>(value) {

    override fun getValue(): T = checkNotNull(super.getValue())
}

@MainThread
fun <X, Y> LiveData<X>.mapNotNull(mapFunction: (X) -> Y?): LiveData<Y> =
    MediatorLiveData<Y>().apply {
        addSource(this@mapNotNull) { x ->
            mapFunction(x)?.let { value = it }
        }
    }
