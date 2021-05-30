package com.jacekpietras.zoo.catalogue.viewmodel

import androidx.lifecycle.ViewModel
import com.jacekpietras.zoo.catalogue.mapper.CatalogueStateMapper
import com.jacekpietras.zoo.catalogue.model.CatalogueListItem
import com.jacekpietras.zoo.catalogue.model.CatalogueState
import com.jacekpietras.zoo.catalogue.model.CatalogueViewState
import com.jacekpietras.zoo.domain.interactor.GetAnimalsByDivisionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class CatalogueViewModel(
    getAnimalsByDivisionUseCase: GetAnimalsByDivisionUseCase,
) : ViewModel() {

    private val mapper = CatalogueStateMapper()

    var state = CatalogueState(
        animalList = getAnimalsByDivisionUseCase()
            .sortedWith(compareBy({ it.regionInZoo }, { it.name }))
    )

    var viewState: Flow<CatalogueViewState> = MutableStateFlow(mapper.from(state))

    fun onAnimalClicked(clickedListItem: CatalogueListItem) {
        TODO("Not yet implemented")
    }
}