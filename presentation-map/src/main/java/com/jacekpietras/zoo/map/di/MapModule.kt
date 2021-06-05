package com.jacekpietras.zoo.map.di

import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mapModule = module {

    factory {
        MapViewStateMapper()
    }

    viewModel { (animalId: String?) ->
        MapViewModel(
            animalId = animalId?.let(::AnimalId),
            viewStateMapper = get(),
            observeWorldBoundsUseCase = get(),
            observeCompassUseCase = get(),
            getBuildingsUseCase = get(),
            observeTakenRouteUseCase = get(),
            getRoadsUseCase = get(),
            getAviaryUseCase = get(),
            getTechnicalRoadsUseCase = get(),
            getLinesUseCase = get(),
            getUserPositionUseCase = get(),
            uploadHistoryUseCase = get(),
            getTerminalNodesUseCase = get(),
            getRegionsInUserPositionUseCase = get(),
            getRegionsContainingPointUseCase = get(),
            getShortestPathUseCase = get(),
            loadAnimalsUseCase = get(),
            getAnimalsInUserPositionUseCase = get(),
        )
    }
}