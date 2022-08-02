package com.jacekpietras.zoo.planner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacekpietras.zoo.core.dispatcher.flowOnBackground
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalEntity
import com.jacekpietras.zoo.domain.feature.favorites.interactor.SetAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.AddExitToCurrentPlanUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.MoveRegionUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.ObserveCurrentPlanStagesWithAnimalsAndOptimizationUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.RemoveRegionFromCurrentPlanUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.SaveRegionImmutableUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.UnseeRegionInCurrentPlanUseCase
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.planner.extensions.reduce
import com.jacekpietras.zoo.planner.mapper.PlannerStateMapper
import com.jacekpietras.zoo.planner.model.PlannerState
import com.jacekpietras.zoo.planner.model.PlannerViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn

internal class PlannerViewModel(
    stateMapper: PlannerStateMapper,
    observeCurrentPlanStagesWithAnimalsAndOptimizationUseCase: ObserveCurrentPlanStagesWithAnimalsAndOptimizationUseCase,
    private val removeRegionFromCurrentPlanUseCase: RemoveRegionFromCurrentPlanUseCase,
    private val unseeRegionInCurrentPlanUseCase: UnseeRegionInCurrentPlanUseCase,
    private val moveRegionUseCase: MoveRegionUseCase,
    private val makeRegionImmutableUseCase: SaveRegionImmutableUseCase,
    private val setAnimalFavoriteUseCase: SetAnimalFavoriteUseCase,
    private val addExitToCurrentPlanUseCase: AddExitToCurrentPlanUseCase,
) : ViewModel() {

    private val planState = observeCurrentPlanStagesWithAnimalsAndOptimizationUseCase.run()
        .flowOnBackground()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val planStateChange = MutableStateFlow<List<Pair<Stage, List<AnimalEntity>>>?>(null)

    private val state = MutableStateFlow(PlannerState())
    val viewState: Flow<PlannerViewState> =
        combine(
            state,
            merge(planStateChange, planState),
            stateMapper::from,
        ).flowOnBackground()

    fun onMove(fromRegionId: String, toRegionId: String) {
        val plan = planState.value!!

        val indexFrom = plan.indexOf(fromRegionId)
        val indexTo = plan.indexOf(toRegionId)
        val elementFrom = plan[indexFrom]

        planStateChange.value = (plan - elementFrom).toMutableList().also { it.add(indexTo, elementFrom) }

        launchInBackground {
            moveRegionUseCase.run(RegionId(fromRegionId), RegionId(toRegionId))
        }
    }

    fun onUnlock(regionId: String) {
        launchInBackground {
            makeRegionImmutableUseCase.run(RegionId(regionId), true)
        }
    }

    fun onUnsee(regionId: String) {
        state.reduce { copy(regionUnderUnseeing = RegionId(regionId)) }
    }

    fun onUnseeDiscarded() {
        state.reduce { copy(regionUnderUnseeing = null) }
    }

    fun onUnseen() {
        state.value.regionUnderUnseeing?.let { regionId ->
            state.reduce { copy(regionUnderUnseeing = null) }
            launchInBackground {
                unseeRegionInCurrentPlanUseCase.run(regionId)
            }
        }
    }

    fun onRemove(regionId: String) {
        launchInBackground {
            getStageWithRegion(regionId)
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

    private fun getStageWithRegion(regionId: String) =
        checkNotNull(planState.value)
            .mapNotNull { (stage, animals) ->
                if (stage is Stage.InRegion) {
                    stage to animals
                } else {
                    null
                }
            }
            .find { (stage, _) -> stage.region.id.id == regionId }

    private fun List<Pair<Stage, List<AnimalEntity>>>.indexOf(regionId: String): Int =
        indexOfFirst { (stage, _) -> stage is Stage.InRegion && stage.region.id.id == regionId }
}