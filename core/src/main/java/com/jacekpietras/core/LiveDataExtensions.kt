package com.jacekpietras.core


import androidx.lifecycle.MutableLiveData

inline fun <T> MutableLiveData<T>.reduce(block: T.() -> T) {
    value = block(checkNotNull(value))
}

inline fun <T> NullSafeMutableLiveData<T>.reduce(block: T.() -> T) {
    value = block(value)
}

class NullSafeMutableLiveData<T>(value: T) : MutableLiveData<T>(value) {

    override fun getValue(): T = checkNotNull(super.getValue())
}

