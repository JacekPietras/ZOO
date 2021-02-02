package com.jacekpietras.zoo.core.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.jacekpietras.zoo.core.extensions.safeValue
import com.jacekpietras.zoo.core.extensions.update

open class BaseViewModel<S, VS>(
    initialState: S,
    mapper: (S) -> VS,
) : ViewModel() {

//    private val state: MutableLiveData<S> = MutableLiveData(initialState)
////    val viewState: LiveData<VS> = Transformations.map(state, mapper)
//    val viewState: VS = mapper(state)
//
//    protected val currentState: S
//        get() = state.safeValue
//
//    protected fun updateState(reduce: S.() -> S) {
//        state.update { reduce(it) }
//    }
}