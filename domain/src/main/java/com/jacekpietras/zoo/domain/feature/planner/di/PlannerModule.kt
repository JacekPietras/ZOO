package com.jacekpietras.zoo.domain.feature.planner.di

import com.jacekpietras.zoo.domain.feature.planner.interactor.AddToCurrentPlanUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.ObserveCurrentPlanPathUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.ObserveCurrentPlanUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.RemoveToCurrentPlanUseCase
import org.koin.dsl.module

val plannerModule = module {
    factory {
        ObserveCurrentPlanUseCase(
            planRepository = get(),
            getAnimalsInRegionUseCase = get(),
            isAnimalInPlanUseCase = get(),
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
    factory {
        ObserveCurrentPlanPathUseCase(
            planRepository = get(),
            calculateShortestPathUseCase = get(),
        )
    }
}
