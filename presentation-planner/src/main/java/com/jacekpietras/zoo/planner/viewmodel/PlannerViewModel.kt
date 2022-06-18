package com.jacekpietras.zoo.planner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.jacekpietras.zoo.core.dispatcher.dispatcherProvider
import com.jacekpietras.zoo.core.extensions.NullSafeMutableLiveData
import com.jacekpietras.zoo.core.extensions.reduceOnMain
import com.jacekpietras.zoo.domain.feature.favorites.interactor.IsAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.ObserveCurrentPlanUseCase
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInRegionUseCase
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.planner.mapper.PlannerStateMapper
import com.jacekpietras.zoo.planner.model.PlannerState
import com.jacekpietras.zoo.planner.model.PlannerViewState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus

internal class PlannerViewModel(
    stateMapper: PlannerStateMapper,
    observeCurrentPlanUseCase: ObserveCurrentPlanUseCase,
    getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
    isAnimalInPlanUseCase: IsAnimalFavoriteUseCase,
) : ViewModel() {

    private val state = NullSafeMutableLiveData(PlannerState())
    var viewState: LiveData<PlannerViewState> = state.map(stateMapper::from)


    init {
        observeCurrentPlanUseCase.run()
            .map {
                it.stages.filterIsInstance<Stage.InRegion>().map { stage ->
                    getAnimalsInRegionUseCase.run(stage.regionId)
                        .filter { isAnimalInPlanUseCase.run(it.id) }
                        .map(AnimalEntity::name).joinToString()
                }
            }
            .onEach {
                state.reduceOnMain { copy(plan = it) }
            }
            .launchIn(viewModelScope + dispatcherProvider.default)
    }
}