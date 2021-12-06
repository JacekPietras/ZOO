package com.jacekpietras.zoo.planner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.extensions.NullSafeMutableLiveData
import com.jacekpietras.zoo.domain.interactor.LoadAnimalsUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveFilteredAnimalsUseCase
import com.jacekpietras.zoo.planner.mapper.PlannerStateMapper
import com.jacekpietras.zoo.planner.model.PlannerState
import com.jacekpietras.zoo.planner.model.PlannerViewState

internal class PlannerViewModel(
    stateMapper: PlannerStateMapper,
) : ViewModel() {

    private val state = NullSafeMutableLiveData(PlannerState())
    var viewState: LiveData<PlannerViewState> = state.map(stateMapper::from)


    init {
        launchInBackground {
//            filterFlow
//                .onEach { onMain { state.reduce { copy(filter = it) } } }
//                .flatMapLatest { observeFilteredAnimalsUseCase.run(it) }
//                .onEach { onMain { state.reduce { copy(animalList = it) } } }
//                .launchIn(this)
        }
    }
}