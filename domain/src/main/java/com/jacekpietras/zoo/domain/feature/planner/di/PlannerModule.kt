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
        AddToCurrentPlanUseCase(
            planRepository = get(),
            getAnimalUseCase = get(),
        )
    }
    factory {
        RemoveFromCurrentPlanUseCase(
            planRepository = get(),
            getAnimalsInRegionUseCase = get(),
            isAnimalFavoriteUseCase = get(),
            getAnimalUseCase = get(),
        )
    }
    factory<ObserveCurrentPlanPathWithOptimizationUseCase> {
        ObserveCurrentPlanPathWithOptimizationUseCaseImpl(
            planRepository = get(),
            mySalesmanProblemSolver = get(),
            gpsRepository = get(),
        )
    }
}
