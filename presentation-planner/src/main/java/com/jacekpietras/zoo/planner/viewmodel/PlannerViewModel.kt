package com.jacekpietras.zoo.planner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.extensions.NullSafeMutableLiveData
import com.jacekpietras.zoo.core.extensions.reduceOnMain
import com.jacekpietras.zoo.domain.feature.planner.interactor.ObserveCurrentPlanUseCase
import com.jacekpietras.zoo.planner.mapper.PlannerStateMapper
import com.jacekpietras.zoo.planner.model.PlannerState
import com.jacekpietras.zoo.planner.model.PlannerViewState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class PlannerViewModel(
    stateMapper: PlannerStateMapper,
    observeCurrentPlanUseCase: ObserveCurrentPlanUseCase,
) : ViewModel() {

    private val state = NullSafeMutableLiveData(PlannerState())
    var viewState: LiveData<PlannerViewState> = state.map(stateMapper::from)


    init {
        launchInBackground {
            observeCurrentPlanUseCase.run()
                .onEach {
                    state.reduceOnMain {
                        copy(
                            plan = it.stages.map { it.animals.joinToString() }
                        )
                    }
                }
                .launchIn(this)
        }
    }
}