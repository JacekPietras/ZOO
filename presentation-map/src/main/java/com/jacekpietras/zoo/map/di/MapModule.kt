package com.jacekpietras.zoo.map.di

import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mapModule = module {

    factory {
        MapViewStateMapper()
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
            observeBuildingsUseCase = get(),
            observeTakenRouteUseCase = get(),
            observeOldTakenRouteUseCase = get(),
            observeRoadsUseCase = get(),
            observeAviaryUseCase = get(),
            observeTechnicalRoadsUseCase = get(),
            observeMapLinesUseCase = get(),
            getUserPositionUseCase = get(),
            uploadHistoryUseCase = get(),
            getTerminalNodesUseCase = get(),
            getShortestPathUseCase = get(),
            loadAnimalsUseCase = get(),
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
        )
    }
}