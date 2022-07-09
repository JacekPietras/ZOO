package com.jacekpietras.zoo.domain.di

import com.jacekpietras.zoo.domain.feature.animal.di.animalModule
import com.jacekpietras.zoo.domain.feature.favorites.di.favoritesModule
import com.jacekpietras.zoo.domain.feature.map.di.mapModule
import com.jacekpietras.zoo.domain.feature.pathfinder.di.pathFinderModule
import com.jacekpietras.zoo.domain.feature.planner.di.plannerModule
import com.jacekpietras.zoo.domain.feature.sensors.di.sensorsModule
import com.jacekpietras.zoo.domain.feature.sensors.interactor.InsertUserPositionUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.InsertUserPositionUseCaseImpl
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveUserPositionUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.UploadHistoryUseCase
import com.jacekpietras.zoo.domain.feature.tsp.di.tspModule
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
        GetAnimalsInRegionUseCase(
            animalRepository = get()
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
    factory<LoadVisitedRouteUseCase> {
        LoadVisitedRouteUseCaseImpl(
            mapRepository = get(),
            gpsRepository = get(),
            initializeGraphAnalyzerIfNeededUseCase = get(),
            pathListSnapper = get(),
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
        ObserveUserPositionUseCase(
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
        IsRegionSeenUseCase(
            mapRepository = get(),
        )
    }
} + favoritesModule + plannerModule + tspModule + pathFinderModule + sensorsModule + mapModule + animalModule