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

internal class PlannerViewModel(
    stateMapper: PlannerStateMapper,
    observeCurrentPlanStagesWithAnimalsAndOptimizationUseCase: ObserveCurrentPlanStagesWithAnimalsAndOptimizationUseCase,
    private val removeRegionFromCurrentPlanUseCase: RemoveRegionFromCurrentPlanUseCase,
    private val moveRegionUseCase: MoveRegionUseCase,
    private val makeRegionImmutableUseCase: MakeRegionImmutableUseCase,
    private val setAnimalFavoriteUseCase: SetAnimalFavoriteUseCase,
    private val addExitToCurrentPlanUseCase: AddExitToCurrentPlanUseCase,
) : ViewModel() {

    private val currentPlan = observeCurrentPlanStagesWithAnimalsAndOptimizationUseCase.run().asLiveData()

    //    private val state = NullSafeMutableLiveData(PlannerState())
//    private val currentState get() = state.value
    var viewState: LiveData<PlannerViewState> = currentPlan.asFlow().map(stateMapper::from).asLiveData()

    init {
//        observeCurrentPlanStagesWithAnimalsAndOptimizationUseCase.run()
//            .onEach { state.reduceOnMain { copy(plan = it) } }
//            .launchIn(viewModelScope + dispatcherProvider.default)
    }

    fun onMove(fromRegionId: String, toRegionId: String) {
        launchInBackground {
//            val indexFrom = indexOfRegionId(fromRegionId)
//            val indexTo = indexOfRegionId(toRegionId)
//            val plan = currentPlan
//            val elementFrom = plan?.get(indexFrom)
//
//            if (plan == null || elementFrom == null) return@launchInBackground
//
//            val newPlan = (plan - elementFrom).toMutableList().also { it.add(indexTo, elementFrom.copy(first = (elementFrom.first as Stage.Single).copy(mutable = false))) }
//            state.reduceOnMain { copy(plan = newPlan) }

            moveRegionUseCase.run(RegionId(fromRegionId), RegionId(toRegionId))
        }
    }

//    private fun indexOfRegionId(regionId: String): Int =
//        currentState.plan?.map { it.first }?.indexOfFirst { it is Stage.InRegion && it.region.id.id == regionId } ?: -1

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