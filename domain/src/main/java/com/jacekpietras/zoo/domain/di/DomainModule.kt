package com.jacekpietras.zoo.domain.di

import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import com.jacekpietras.zoo.domain.business.PathListSnapper
import com.jacekpietras.zoo.domain.business.PathSnapper
import com.jacekpietras.zoo.domain.feature.favorites.di.favoritesModule
import com.jacekpietras.zoo.domain.feature.planner.di.plannerModule
import com.jacekpietras.zoo.domain.interactor.*
import org.koin.dsl.module

val domainModule = module {

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
        StopLightSensorUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        StartLightSensorUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        StopNavigationUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        StartNavigationUseCase(
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
        GetAnimalPositionUseCase(
            getAnimalUseCase = get(),
            getRegionCenterPointUseCase = get(),
        )
    }
    factory {
        FindNearRegionWithDistanceUseCase(
            findRegionUseCase = get(),
            getRegionCenterPointUseCase = get(),
            getShortestPathUseCase = get(),
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
        ObserveRegionsInUserPositionUseCase(
            getRegionsContainingPointUseCase = get(),
            gpsRepository = get(),
        )
    }
    factory {
        ObserveRegionsWithAnimalsInUserPositionUseCase(
            observeRegionsInUserPositionUseCase = get(),
            getAnimalsInRegionUseCase = get(),
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
            getUserPositionUseCase = get(),
            initializeGraphAnalyzerIfNeededUseCase = get(),
            graphAnalyzer = get(),
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
        ObserveSuggestedThemeTypeUseCase(
            observeRegionsInUserPositionUseCase = get(),
            startLightSensorUseCase = get(),
            stopLightSensorUseCase = get(),
            mapRepository = get(),
            gpsRepository = get(),
        )
    }
    factory {
        LoadMapUseCase(
            mapRepository = get(),
        )
    }
    factory<LoadVisitedRouteUseCase> {
        LoadVisitedRouteUseCaseImpl(
            mapRepository = get(),
            gpsRepository = get(),
            initializeGraphAnalyzerIfNeededUseCase = get(),
            pathListSnapper = get(),
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
        GetUserPositionUseCase(
            gpsRepository = get(),
        )
    }
    factory<InsertUserPositionUseCase> {
        InsertUserPositionUseCaseImpl(
            gpsRepository = get(),
            mapRepository = get(),
            worldBoundsUseCase = get(),
            stopNavigationUseCase = get(),
            pathListSnapper = get(),
            pathSnapper = get(),
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
    factory<GetTerminalNodesUseCase> {
        GetTerminalNodesUseCaseImpl(
            initializeGraphAnalyzerIfNeededUseCase = get(),
            graphAnalyzer = get(),
        )
    }
    factory {
        IsAnimalSeenUseCase(
            getAnimalUseCase = get(),
            isRegionSeenUseCase = get(),
        )
    }
    factory {
        IsRegionSeenUseCase(
            mapRepository = get(),
        )
    }
} + favoritesModule + plannerModule