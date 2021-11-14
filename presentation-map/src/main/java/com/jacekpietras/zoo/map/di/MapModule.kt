package com.jacekpietras.zoo.map.di

import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mapModule = module {

    factory {
        MapViewStateMapper(
            regionMapper = get()
        )
    }

    viewModel { (animalId: String?, regionId: String?) ->
        MapViewModel(
            animalId = animalId
                ?.takeIf { it.isNotBlank() }
                ?.takeIf { it != "null" }
                ?.let(::AnimalId),
            regionId = regionId
                ?.takeIf { it.isNotBlank() }
                ?.takeIf { it != "null" }
                ?.let(::RegionId),
            mapper = get(),
            observeWorldBoundsUseCase = get(),
            observeCompassUseCase = get(),
            getBuildingsUseCase = get(),
            observeTakenRouteUseCase = get(),
            observeOldTakenRouteUseCase = get(),
            getRoadsUseCase = get(),
            getAviaryUseCase = get(),
            getTechnicalRoadsUseCase = get(),
            getLinesUseCase = get(),
            getUserPositionUseCase = get(),
            uploadHistoryUseCase = get(),
            getTerminalNodesUseCase = get(),
            getShortestPathUseCase = get(),
            loadAnimalsUseCase = get(),
            getRegionsWithAnimalsInUserPositionUseCase = get(),
            getAnimalUseCase = get(),
            getRegionCenterPointUseCase = get(),
            getRegionsContainingPointUseCase = get(),
            getAnimalsInRegionUseCase = get(),
            stopCompassUseCase = get(),
            startCompassUseCase = get(),
            findRegionUseCase = get(),
        )
    }
}