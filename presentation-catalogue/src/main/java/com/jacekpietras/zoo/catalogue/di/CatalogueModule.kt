package com.jacekpietras.zoo.catalogue.di

import com.jacekpietras.zoo.catalogue.viewmodel.CatalogueViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val catalogueModule = module {

    viewModel {
        CatalogueViewModel(
            getAnimalsByDivisionUseCase = get(),
        )
    }
}