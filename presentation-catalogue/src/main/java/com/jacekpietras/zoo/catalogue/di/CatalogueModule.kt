package com.jacekpietras.zoo.catalogue.di

import com.jacekpietras.zoo.catalogue.feature.animal.viewmodel.AnimalViewModel
import com.jacekpietras.zoo.catalogue.feature.list.mapper.CatalogueStateMapper
import com.jacekpietras.zoo.catalogue.feature.list.mapper.DivisionMapper
import com.jacekpietras.zoo.catalogue.feature.list.viewmodel.CatalogueViewModel
import com.jacekpietras.zoo.domain.model.AnimalId
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val catalogueModule = module {

    viewModel {
        CatalogueViewModel(
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
        )
    }

    factory { DivisionMapper() }
    factory {
        CatalogueStateMapper(
            divisionMapper = get(),
        )
    }
}