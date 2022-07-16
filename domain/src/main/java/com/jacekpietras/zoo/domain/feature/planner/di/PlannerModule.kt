package com.jacekpietras.zoo.domain.feature.planner.di

import com.jacekpietras.zoo.domain.feature.planner.interactor.*
import org.koin.dsl.module

val plannerModule = module {
    factory {
        ObserveCurrentPlanUseCase(
            planRepository = get(),
        )
    }
    factory {
        GetOrCreateCurrentPlanUseCase(
            planRepository = get(),
        )
    }
    factory {
        AddAnimalToCurrentPlanUseCase(
            planRepository = get(),
            getAnimalUseCase = get(),
            getOrCreateCurrentPlanUseCase = get(),
            isAnimalSeenUseCase = get(),
        )
    }
    factory {
        MakeRegionImmutableUseCase()
    }
    factory {
        SaveRegionImmutableUseCase(
            makeRegionImmutableUseCase = get(),
            getOrCreateCurrentPlanUseCase = get(),
            planRepository = get(),
        )
    }
    factory {
        MoveRegionUseCase(
            planRepository = get(),
            makeRegionImmutableUseCase = get(),
            getOrCreateCurrentPlanUseCase = get(),
        )
    }
    factory {
        AddStageToCurrentPlanUseCase(
            planRepository = get(),
            getOrCreateCurrentPlanUseCase = get(),
        )
    }
    factory {
        AddExitToCurrentPlanUseCase(
            addStageToCurrentPlanUseCase = get(),
            findRegionUseCase = get(),
        )
    }
    factory {
        RemoveAnimalFromCurrentPlanUseCase(
            planRepository = get(),
            getAnimalsInRegionUseCase = get(),
            isAnimalFavoriteUseCase = get(),
            getAnimalUseCase = get(),
            getOrCreateCurrentPlanUseCase = get(),
        )
    }
    factory {
        RemoveRegionFromCurrentPlanUseCase(
            planRepository = get(),
            getOrCreateCurrentPlanUseCase = get(),
        )
    }
    factory<ObserveCurrentPlanWithOptimizationUseCase> {
        ObserveCurrentPlanWithOptimizationUseCaseImpl(
            planRepository = get(),
            tspSolver = get(),
            gpsRepository = get(),
            observeCurrentPlanUseCase = get(),
        )
    }
    factory {
        ObserveCurrentPlanPathWithOptimizationUseCase(
            observeCurrentPlanWithOptimizationUseCase = get(),
        )
    }
    factory {
        ObserveCurrentPlanStagesWithAnimalsAndOptimizationUseCase(
            observeCurrentPlanWithOptimizationUseCase = get(),
            getAnimalsInRegionUseCase = get(),
            observeAnimalFavoritesUseCase = get(),
        )
    }
}
