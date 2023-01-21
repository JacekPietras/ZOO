package com.jacekpietras.zoo.map.di

import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mapModule = module {

    factory {
        MapViewStateMapper()
    }

    viewModel { params ->
        MapViewModel<Any>(
            context = androidContext(),
            animalId = params[0],
            regionId = params[1],
            paintBaker = params.get(),
            mapper = get(),
            observeMapObjectsUseCase = get(),
            observeCompassUseCase = get(),
            observeTakenRouteUseCase = get(),
            observeUserPositionWithAccuracyUseCase = get(),
            uploadHistoryUseCase = get(),
            getShortestPathUseCase = get(),
            observeOutsideWorldEventUseCase = get(),
            observeRegionsWithAnimalsInUserPositionUseCase = get(),
            getAnimalUseCase = get(),
            findNearRegionWithDistance = get(),
            getRegionsContainingPointUseCase = get(),
            getRegionUseCase = get(),
            getAnimalsInRegionUseCase = get(),
            stopCompassUseCase = get(),
            startCompassUseCase = get(),
            startNavigationUseCase = get(),
            observeVisitedRoadsUseCase = get(),
            loadMapUseCase = get(),
            loadVisitedRouteUseCase = get(),
            observeSuggestedThemeTypeUseCase = get(),
            observeCurrentPlanPathUseCase = get(),
            observeArrivalAtRegionEventUseCase = get(),
            observeAnimalFavoritesUseCase = get(),
        )
    }
}