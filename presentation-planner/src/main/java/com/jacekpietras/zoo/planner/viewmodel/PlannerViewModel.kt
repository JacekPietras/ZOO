package com.jacekpietras.zoo.planner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.domain.feature.favorites.interactor.SetAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.*
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.planner.mapper.PlannerStateMapper
import com.jacekpietras.zoo.planner.model.PlannerViewState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal class PlannerViewModel(
    stateMapper: PlannerStateMapper,
    observeCurrentPlanStagesWithAnimalsAndOptimizationUseCase: ObserveCurrentPlanStagesWithAnimalsAndOptimizationUseCase,
    private val removeRegionFromCurrentPlanUseCase: RemoveRegionFromCurrentPlanUseCase,
    private val moveRegionUseCase: MoveRegionUseCase,
    private val makeRegionImmutableUseCase: SaveRegionImmutableUseCase,
    private val setAnimalFavoriteUseCase: SetAnimalFavoriteUseCase,
    private val addExitToCurrentPlanUseCase: AddExitToCurrentPlanUseCase,
) : ViewModel() {

    private val currentPlan = observeCurrentPlanStagesWithAnimalsAndOptimizationUseCase.run().asLiveData()
    var viewState: LiveData<PlannerViewState> =
        currentPlan
            .asFlow()
            .onStart { emit(emptyList()) }
            .map(stateMapper::from)
            .asLiveData()

    fun onMove(fromRegionId: String, toRegionId: String) {
        launchInBackground {
            moveRegionUseCase.run(RegionId(fromRegionId), RegionId(toRegionId))
        }
    }

    fun onUnlock(regionId: String) {
        launchInBackground {
            makeRegionImmutableUseCase.run(RegionId(regionId), true)
        }
    }

    fun onRemove(regionId: String) {
        launchInBackground {
            currentPlan.value
                ?.mapNotNull { (stage, animals) ->
                    if (stage is Stage.InRegion) {
                        stage to animals
                    } else {
                        null
                    }
                }
                ?.find { (stage, _) -> stage.region.id.id == regionId }
                ?.let { (stage, animals) ->
                    animals.forEach { animal ->
                        setAnimalFavoriteUseCase.run(animal.id, false)
                    }
                    removeRegionFromCurrentPlanUseCase.run(stage)
                }
        }
    }

    fun onAddExitClicked() {
        launchInBackground {
            addExitToCurrentPlanUseCase.run()
        }
    }
}