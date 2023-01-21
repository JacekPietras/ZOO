package com.jacekpietras.zoo.catalogue.di

import com.jacekpietras.zoo.catalogue.feature.animal.viewmodel.AnimalViewModel
import com.jacekpietras.zoo.catalogue.feature.list.mapper.CatalogueStateMapper
import com.jacekpietras.zoo.catalogue.feature.list.mapper.DivisionMapper
import com.jacekpietras.zoo.catalogue.feature.list.viewmodel.CatalogueViewModel
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val catalogueModule = module {

    viewModel { params ->
        CatalogueViewModel(
            regionId = params[0],
            observeFilteredAnimalsUseCase = get(),
            stateMapper = get(),
            divisionMapper = get(),
        )
    }

    viewModel { params ->
        AnimalViewModel(
            animalId = AnimalId(params[0]),
            paintBaker = params.get(),
            getAnimalUseCase = get(),
            isAnimalSeenUseCase = get(),
            observeAnimalFavoritesUseCase = get(),
            setAnimalFavoriteUseCase = get(),
            addAnimalToCurrentPlanUseCase = get(),
            removeFromCurrentPlanUseCase = get(),
            observeWorldBoundsUseCase = get(),
            observeBuildingsUseCase = get(),
            observeAviaryUseCase = get(),
            observeRoadsUseCase = get(),
            getAnimalPositionUseCase = get(),
            observeUserPositionUseCase = get(),
            getShortestPathUseCase = get(),
            startNavigationUseCase = get(),
        )
    }

    factory { DivisionMapper() }
    factory {
        CatalogueStateMapper(
            divisionMapper = get(),
        )
    }
}