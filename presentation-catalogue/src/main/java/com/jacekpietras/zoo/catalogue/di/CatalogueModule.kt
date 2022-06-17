package com.jacekpietras.zoo.catalogue.di

import com.jacekpietras.zoo.catalogue.feature.animal.viewmodel.AnimalViewModel
import com.jacekpietras.zoo.catalogue.feature.list.mapper.CatalogueStateMapper
import com.jacekpietras.zoo.catalogue.feature.list.mapper.DivisionMapper
import com.jacekpietras.zoo.catalogue.feature.list.viewmodel.CatalogueViewModel
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val catalogueModule = module {

    viewModel { (regionId: String?) ->
        CatalogueViewModel(
            regionId = regionId
                ?.takeIf { it.isNotBlank() }
                ?.takeIf { it != "null" }
                ?.let(::RegionId),
            observeFilteredAnimalsUseCase = get(),
            loadAnimalsUseCase = get(),
            stateMapper = get(),
            divisionMapper = get(),
        )
    }

    viewModel { (animalId: String) ->
        AnimalViewModel(
            animalId = AnimalId(animalId),
            getAnimalUseCase = get(),
            isAnimalSeenUseCase = get(),
            isAnimalFavoriteUseCase = get(),
            setAnimalFavoriteUseCase = get(),
            addToCurrentPlanUseCase = get(),
            removeToCurrentPlanUseCase = get(),
            observeWorldBoundsUseCase = get(),
            observeBuildingsUseCase = get(),
            observeAviaryUseCase = get(),
            observeRoadsUseCase = get(),
            getAnimalPositionUseCase = get(),
            observeUserPositionUseCase = get(),
            getShortestPathUseCase = get(),
        )
    }

    factory { DivisionMapper() }
    factory {
        CatalogueStateMapper(
            divisionMapper = get(),
        )
    }
}