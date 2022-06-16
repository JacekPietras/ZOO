package com.jacekpietras.zoo.domain.feature.planner.di

import com.jacekpietras.zoo.domain.feature.planner.interactor.ObserveCurrentPlanUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.AddToCurrentPlanUseCase
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
}
