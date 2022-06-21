package com.jacekpietras.zoo.planner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.jacekpietras.zoo.core.dispatcher.dispatcherProvider
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.extensions.NullSafeMutableLiveData
import com.jacekpietras.zoo.core.extensions.reduceOnMain
import com.jacekpietras.zoo.domain.feature.favorites.interactor.ObserveAnimalFavoritesUseCase
import com.jacekpietras.zoo.domain.feature.favorites.interactor.SetAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.ObserveCurrentPlanUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.RemoveRegionFromCurrentPlanUseCase
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInRegionUseCase
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.planner.mapper.PlannerStateMapper
import com.jacekpietras.zoo.planner.model.PlannerState
import com.jacekpietras.zoo.planner.model.PlannerViewState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus

internal class PlannerViewModel(
    stateMapper: PlannerStateMapper,
    observeCurrentPlanUseCase: ObserveCurrentPlanUseCase,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
    private val removeRegionFromCurrentPlanUseCase: RemoveRegionFromCurrentPlanUseCase,
    observeAnimalFavoritesUseCase: ObserveAnimalFavoritesUseCase,
    private val setAnimalFavoriteUseCase: SetAnimalFavoriteUseCase,
) : ViewModel() {

    private val state = NullSafeMutableLiveData(PlannerState())
    private val currentState get() = state.value
    var viewState: LiveData<PlannerViewState> = state.map(stateMapper::from)

    init {
        observeCurrentPlanUseCase.run()
            .combine(observeAnimalFavoritesUseCase.run()) { plan, favorites ->
                plan.stages
                    .map { stage ->
                        stage to stage.getAnimals(favorites)
                    }
            }
            .onEach {
                state.reduceOnMain { copy(plan = it) }
            }
            .launchIn(viewModelScope + dispatcherProvider.default)
    }

    private fun Stage.getAnimals(favorites: List<AnimalId>): List<AnimalEntity> =
        if (this is Stage.InRegion) {
            getAnimalsInRegionUseCase.run(regionId)
                .filter { animal -> favorites.contains(animal.id) }
        } else {
            emptyList()
        }

    fun onRemove(regionId: String) {
        launchInBackground {
            currentState.plan
                .mapNotNull { (stage, animals) ->
                    if (stage is Stage.InRegion) {
                        stage to animals
                    } else {
                        null
                    }
                }
                .find { (stage, _) -> stage.regionId.id == regionId }
                ?.let { (stage, animals) ->
                    animals.forEach { animal ->
                        setAnimalFavoriteUseCase.run(animal.id, false)
                    }
                    removeRegionFromCurrentPlanUseCase.run(stage)
                }
        }
    }
}