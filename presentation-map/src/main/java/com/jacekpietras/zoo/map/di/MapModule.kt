package com.jacekpietras.zoo.map.di

import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.service.TrackingServiceStarter
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mapModule = module {

    factory {
        MapViewStateMapper()
    }
    factory {
        TrackingServiceStarter(
            context = androidApplication()
        )
    }

    viewModel { params ->
        MapViewModel(
            animalId = params[0],
            regionId = params[1],
            mapper = get(),
            observeWorldBoundsUseCase = get(),
            observeCompassUseCase = get(),
            observeBuildingsUseCase = get(),
            observeTakenRouteUseCase = get(),
            observeOldTakenRouteUseCase = get(),
            observeRoadsUseCase = get(),
            observeAviaryUseCase = get(),
            observeTechnicalRoadsUseCase = get(),
            observeMapLinesUseCase = get(),
            observeUserPositionUseCase = get(),
            uploadHistoryUseCase = get(),
            getShortestPathUseCase = get(),
            observeRegionsWithAnimalsInUserPositionUseCase = get(),
            getAnimalUseCase = get(),
            findNearRegionWithDistance = get(),
            getRegionsContainingPointUseCase = get(),
            getAnimalsInRegionUseCase = get(),
            stopCompassUseCase = get(),
            startCompassUseCase = get(),
            startNavigationUseCase = get(),
            observeVisitedRoadsUseCase = get(),
            loadMapUseCase = get(),
            loadVisitedRouteUseCase = get(),
            observeSuggestedThemeTypeUseCase = get(),
            observeCurrentPlanPathUseCase = get(),
            trackingServiceStarter = get(),
        )
    }
}