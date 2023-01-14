package com.jacekpietras.zoo.domain.di

import com.jacekpietras.zoo.domain.feature.animal.di.animalModule
import com.jacekpietras.zoo.domain.feature.favorites.di.favoritesModule
import com.jacekpietras.zoo.domain.feature.map.di.mapModule
import com.jacekpietras.zoo.domain.feature.pathfinder.di.pathFinderModule
import com.jacekpietras.zoo.domain.feature.planner.di.plannerModule
import com.jacekpietras.zoo.domain.feature.sensors.di.sensorsModule
import com.jacekpietras.zoo.domain.feature.vrp.di.vrpModule
import com.jacekpietras.zoo.domain.interactor.FindNearRegionWithDistanceUseCase
import com.jacekpietras.zoo.domain.interactor.FindRegionUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInRegionUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionCenterPointUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionsContainingPointUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionsInUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.IsRegionSeenUseCase
import com.jacekpietras.zoo.domain.interactor.LoadVisitedRouteUseCase
import com.jacekpietras.zoo.domain.interactor.LoadVisitedRouteUseCaseImpl
import com.jacekpietras.zoo.domain.interactor.ObserveOldTakenRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveRegionCentersUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveRegionsInUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveRegionsWithAnimalsInUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveSuggestedThemeTypeUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveTakenRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveVisitedRoadsUseCase
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
            getRegionUseCase = get(),
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
        ObserveRegionCentersUseCase(
            mapRepository = get(),
        )
    }
    factory {
        IsRegionSeenUseCase(
            mapRepository = get(),
            getRegionUseCase = get(),
        )
    }
    factory {
        GetRegionUseCase(
            mapRepository = get(),
        )
    }
} + favoritesModule + plannerModule + vrpModule + pathFinderModule + sensorsModule + mapModule + animalModule