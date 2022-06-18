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
        )
    }
    factory {
        RemoveToCurrentPlanUseCase(
            planRepository = get(),
            getAnimalsInRegionUseCase = get(),
            isAnimalFavoriteUseCase = get(),
        )
    }
    factory<ObserveCurrentPlanPathUseCase> {
        ObserveCurrentPlanPathUseCaseImpl(
            planRepository = get(),
            mySalesmanProblemSolver = get(),
            gpsRepository = get(),
        )
    }
}
