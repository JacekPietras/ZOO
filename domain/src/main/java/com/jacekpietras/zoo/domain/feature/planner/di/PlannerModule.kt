package com.jacekpietras.zoo.domain.feature.planner.di

import com.jacekpietras.zoo.domain.feature.planner.interactor.*
import org.koin.dsl.module
import kotlin.time.ExperimentalTime

@ExperimentalTime
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
    factory<ObserveCurrentPlanPathWithOptimizationUseCase> {
        ObserveCurrentPlanPathWithOptimizationUseCaseImpl(
            planRepository = get(),
            tspSolver = get(),
            gpsRepository = get(),
        )
    }
}
