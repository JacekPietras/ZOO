package com.jacekpietras.zoo.domain.feature.pathfinder.di

import com.jacekpietras.zoo.domain.feature.pathfinder.intractor.CalculateShortestPathUseCase
import com.jacekpietras.zoo.domain.feature.pathfinder.intractor.CalculateShortestPathUseCaseImpl
import org.koin.dsl.module

val pathFinderModule = module {
    factory<CalculateShortestPathUseCase> {
        CalculateShortestPathUseCaseImpl(
        )
    }
}