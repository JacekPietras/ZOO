package com.jacekpietras.zoo.catalogue.feature.list.viewmodel

import androidx.lifecycle.ViewModel
import com.jacekpietras.zoo.catalogue.feature.list.mapper.CatalogueStateMapper
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueState
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueViewState
import com.jacekpietras.zoo.catalogue.feature.list.router.CatalogueRouter
import com.jacekpietras.zoo.domain.interactor.GetAnimalsByDivisionUseCase
import com.jacekpietras.zoo.domain.model.AnimalId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class CatalogueViewModel(
    getAnimalsByDivisionUseCase: GetAnimalsByDivisionUseCase,
) : ViewModel() {

    private val mapper = CatalogueStateMapper()
    private val state = CatalogueState(
        animalList = getAnimalsByDivisionUseCase()
            .sortedWith(compareBy({ it.regionInZoo }, { it.name }))
    )
    var viewState: Flow<CatalogueViewState> = MutableStateFlow(mapper.from(state))

    fun onAnimalClicked(animalId: String, router: CatalogueRouter) {
        router.navigateToAnimal(AnimalId(animalId))
    }
}