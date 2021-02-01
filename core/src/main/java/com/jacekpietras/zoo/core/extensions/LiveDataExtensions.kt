package com.jacekpietras.zoo.core.extensions

import androidx.lifecycle.MutableLiveData

val <T> MutableLiveData<T>.safeValue: T
    get() = checkNotNull(value)

fun <T> MutableLiveData<T>.update(block: (T) -> T) {
    value = block(safeValue)
}
