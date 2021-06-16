package com.jacekpietras.zoo.catalogue.feature.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.jacekpietras.core.NullSafeMutableLiveData
import com.jacekpietras.core.reduce
import com.jacekpietras.zoo.catalogue.feature.list.mapper.CatalogueStateMapper
import com.jacekpietras.zoo.catalogue.feature.list.mapper.DivisionMapper
import com.jacekpietras.zoo.catalogue.feature.list.model.AnimalDivision
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueState
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueViewState
import com.jacekpietras.zoo.catalogue.feature.list.router.CatalogueRouter
import com.jacekpietras.zoo.core.dispatcher.DefaultDispatcherProvider
import com.jacekpietras.zoo.core.dispatcher.DispatcherProvider
import com.jacekpietras.zoo.domain.interactor.LoadAnimalsUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveFilteredAnimalsUseCase
import com.jacekpietras.zoo.domain.model.AnimalFilter
import com.jacekpietras.zoo.domain.model.AnimalId
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class CatalogueViewModel(
    private val observeFilteredAnimalsUseCase: ObserveFilteredAnimalsUseCase,
    private val stateMapper: CatalogueStateMapper,
    private val divisionMapper: DivisionMapper,
    loadAnimalsUseCase: LoadAnimalsUseCase,
    dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
) : ViewModel() {

    private val state = NullSafeMutableLiveData(CatalogueState())
    var viewState: Flow<CatalogueViewState> = state.map(stateMapper::from).asFlow()

    private val filterFlow = MutableStateFlow(AnimalFilter())

    init {
        viewModelScope.launch(dispatcherProvider.main) {
            launch { loadAnimalsUseCase.run() }

            filterFlow
                .onEach { state.reduce { copy(filter = it) } }
                .flatMapLatest { observeFilteredAnimalsUseCase.run(it) }
                .onEach { state.reduce { copy(animalList = it) } }
                .launchIn(this)
        }
    }

    fun onAnimalClicked(animalId: String, router: CatalogueRouter) {
        router.navigateToAnimal(AnimalId(animalId))
    }

    fun onFilterClicked(division: AnimalDivision) {
        val domainDivision = divisionMapper.from(division)
        filterFlow.value = with(filterFlow.value) {
            if (divisions.contains(domainDivision)) {
                copy(divisions = divisions - domainDivision)
            } else {
                copy(divisions = divisions + domainDivision)
            }
        }
    }

    fun onSearchClicked() {
        state.reduce { copy(searchOpened = !searchOpened) }
    }

    fun onSearch(query: String) {
        filterFlow.value = filterFlow.value.copy(query = query)
    }
}