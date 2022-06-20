package com.jacekpietras.zoo.domain.feature.map.di

import com.jacekpietras.zoo.domain.feature.map.interactor.*
import org.koin.dsl.module

val mapModule = module {
    factory {
        LoadMapUseCase(
            mapRepository = get(),
        )
    }
    factory {
        ObserveWorldBoundsUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetWorldBoundsUseCase(
            mapRepository = get()
        )
    }
    factory {
        ObserveBuildingsUseCase(
            mapRepository = get()
        )
    }
    factory {
        ObserveAviaryUseCase(
            mapRepository = get()
        )
    }
    factory {
        ObserveRoadsUseCase(
            mapRepository = get()
        )
    }
    factory {
        ObserveMapLinesUseCase(
            mapRepository = get(),
        )
    }
    factory<GetTerminalNodesUseCase> {
        GetTerminalNodesUseCaseImpl(
            initializeGraphAnalyzerIfNeededUseCase = get(),
            graphAnalyzer = get(),
        )
    }
    factory {
        ObserveTechnicalRoadsUseCase(
            mapRepository = get(),
        )
    }
}
