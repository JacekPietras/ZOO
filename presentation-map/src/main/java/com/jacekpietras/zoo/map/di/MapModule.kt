package com.jacekpietras.zoo.map.di

import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mapModule = module {

    factory {
        MapViewStateMapper()
    }

    viewModel {
        MapViewModel(
            viewStateMapper = get(),
            getWorldBoundsUseCase = get(),
            getBuildingsUseCase = get(),
            getTakenRouteUseCase = get(),
            getRoadsUseCase = get(),
            getUserPositionUseCase = get(),
            uploadHistoryUseCase = get(),
            getRegionsInUserPositionUseCase = get(),
        )
    }
}