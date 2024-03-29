package com.jacekpietras.zoo.planner.di

import com.jacekpietras.zoo.planner.mapper.PlannerStateMapper
import com.jacekpietras.zoo.planner.viewmodel.PlannerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val plannerModule = module {

    viewModel {
        PlannerViewModel(
            stateMapper = get(),
            observeCurrentPlanStagesWithAnimalsAndOptimizationUseCase = get(),
            addExitToCurrentPlanUseCase = get(),
            removeRegionFromCurrentPlanUseCase = get(),
            setAnimalFavoriteUseCase = get(),
            moveRegionUseCase = get(),
            makeRegionImmutableUseCase = get(),
            unseeRegionInCurrentPlanUseCase = get(),
        )
    }

    factory { PlannerStateMapper() }
}
