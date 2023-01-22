package com.jacekpietras.zoo.domain.feature.performance.di

import com.jacekpietras.zoo.domain.feature.performance.interactor.GetPerformanceClassUseCase
import org.koin.dsl.module

val performanceModule = module {
    factory {
        GetPerformanceClassUseCase(
            performanceClassRepository = get(),
        )
    }
}