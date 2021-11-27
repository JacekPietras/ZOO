package com.jacekpietras.zoo.domain.di

import com.jacekpietras.zoo.domain.interactor.*
import org.koin.dsl.module

val domainModule = module {

    factory {
        GetRegionsInUserPositionUseCase(
            getRegionsContainingPointUseCase = get(),
            gpsRepository = get(),
        )
    }
    factory {
        StopCompassUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        StartCompassUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        FindRegionUseCase(
            mapRepository = get(),
        )
    }
    factory {
        GetRegionsContainingPointUseCase(
            mapRepository = get(),
        )
    }
    factory {
        GetRegionCenterPointUseCase(
            mapRepository = get(),
        )
    }
    factory {
        ObserveBuildingsUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetAnimalsInRegionUseCase(
            animalRepository = get()
        )
    }
    factory {
        ObserveFilteredAnimalsUseCase(
            animalRepository = get()
        )
    }
    factory {
        GetAnimalUseCase(
            animalRepository = get()
        )
    }
    factory {
        GetAnimalsByDivisionUseCase(
            animalRepository = get()
        )
    }
    factory {
        GetAnimalsInUserPositionUseCase(
            getAnimalsInRegionUseCase = get(),
            getRegionsInUserPositionUseCase = get(),
        )
    }
    factory {
        ObserveRegionsWithAnimalsInUserPositionUseCase(
            getRegionsContainingPointUseCase = get(),
            getAnimalsInRegionUseCase = get(),
            gpsRepository = get(),
        )
    }
    factory {
        ObserveAviaryUseCase(
            mapRepository = get()
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
        ObserveRoadsUseCase(
            mapRepository = get()
        )
    }
    factory {
        InitializeGraphAnalyzerIfNeededUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetShortestPathUseCase(
            initializeGraphAnalyzerIfNeededUseCase = get(),
        )
    }
    factory {
        GetShortestPathFromUserUseCase(
            getUserPositionUseCase = get(),
            initializeGraphAnalyzerIfNeededUseCase = get(),
        )
    }
    factory {
        ObserveTechnicalRoadsUseCase(
            mapRepository = get(),
        )
    }
    factory {
        ObserveVisitedRoadsUseCase(
            mapRepository = get(),
        )
    }
    factory {
        LoadMapUseCase(
            mapRepository = get(),
        )
    }
    factory {
        LoadVisitedRouteUseCase(
            mapRepository = get(),
            gpsRepository = get(),
            getSnapPathToRoadUseCase = get(),
        )
    }
    factory {
        ObserveMapLinesUseCase(
            mapRepository = get(),
        )
    }
    factory {
        LoadAnimalsUseCase(
            animalRepository = get(),
        )
    }
    factory {
        ObserveTakenRouteUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        ObserveOldTakenRouteUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        GetSnapPathToRoadUseCase(
            initializeGraphAnalyzerIfNeededUseCase = get(),
        )
    }
    factory {
        GetUserPositionUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        InsertUserPositionUseCase(
            gpsRepository = get(),
            worldBoundsUseCase = get(),
        )
    }
    factory {
        UploadHistoryUseCase(
            mailGateway = get(),
            gpsRepository = get(),
        )
    }
    factory {
        ObserveCompassUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        InsertUserCompassUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        GetTerminalNodesUseCase(
            initializeGraphAnalyzerIfNeededUseCase = get(),
        )
    }
}