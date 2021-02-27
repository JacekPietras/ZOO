package com.jacekpietras.zoo.catalogue.viewmodel

import androidx.lifecycle.ViewModel
import com.jacekpietras.zoo.domain.interactor.GetAnimalsByDivisionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CatalogueViewModel(
    getAnimalsByDivisionUseCase: GetAnimalsByDivisionUseCase,
) : ViewModel() {

    var animalListExplicit: List<String> = getAnimalsByDivisionUseCase()
        .sortedWith(compareBy({ it.regionInZoo }, { it.name }))
        .map { "${it.name} (${it.regionInZoo})" }

    var viewState: Flow<List<String>> =
        flow { animalListExplicit }
}