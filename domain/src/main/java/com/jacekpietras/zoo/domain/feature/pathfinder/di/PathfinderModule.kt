package com.jacekpietras.zoo.domain.feature.pathfinder.di

import com.jacekpietras.zoo.domain.feature.pathfinder.GraphAnalyzer
import com.jacekpietras.zoo.domain.feature.pathfinder.PathListSnapper
import com.jacekpietras.zoo.domain.feature.pathfinder.PathSnapper
import com.jacekpietras.zoo.domain.feature.pathfinder.interactor.*
import org.koin.dsl.module

val pathFinderModule = module {
    single {
        GraphAnalyzer()
    }
    factory {
        PathSnapper(
            graphAnalyzer = get(),
        )
    }
    factory {
        PathListSnapper(
            pathSnapper = get(),
        )
    }
    factory<InitializeGraphAnalyzerIfNeededUseCase> {
        InitializeGraphAnalyzerIfNeededUseCaseImpl(
            mapRepository = get(),
            graphAnalyzer = get(),
        )
    }
    factory<GetShortestPathUseCase> {
        GetShortestPathUseCaseImpl(
            initializeGraphAnalyzerIfNeededUseCase = get(),
            graphAnalyzer = get(),
        )
    }
    factory<GetShortestPathFromUserUseCase> {
        GetShortestPathFromUserUseCaseImpl(
            observeUserPositionUseCase = get(),
            initializeGraphAnalyzerIfNeededUseCase = get(),
            graphAnalyzer = get(),
        )
    }
}