package com.jacekpietras.zoo.domain.feature.map.di

import com.jacekpietras.zoo.domain.feature.map.interactor.GetTerminalNodesUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.GetTerminalNodesUseCaseImpl
import com.jacekpietras.zoo.domain.feature.map.interactor.GetWorldBoundsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.LoadMapUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveAviaryUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveBuildingsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveForestUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveMapLinesUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveMapObjectsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveRoadsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveTechnicalRoadsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveTerminalNodesUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveWaterUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveWorldBoundsUseCase
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
        ObserveForestUseCase(
            mapRepository = get()
        )
    }
    factory {
        ObserveWaterUseCase(
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
        ObserveTerminalNodesUseCase(
            getTerminalNodesUseCase = get(),
        )
    }
    factory {
        ObserveTechnicalRoadsUseCase(
            mapRepository = get(),
        )
    }
    factory {
        ObserveMapObjectsUseCase(
            observeWorldBoundsUseCase = get(),
            observeBuildingsUseCase = get(),
            observeAviaryUseCase = get(),
            observeRoadsUseCase = get(),
            observeWaterUseCase = get(),
            observeForestUseCase = get(),
            observeTechnicalRoadsUseCase = get(),
            observeOldTakenRouteUseCase = get(),
            observeRegionCentersUseCase = get(),
            observeMapLinesUseCase = get(),
        )
    }
}
