package com.jacekpietras.zoo.catalogue.feature.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import com.jacekpietras.core.NullSafeMutableLiveData
import com.jacekpietras.core.reduce
import com.jacekpietras.zoo.catalogue.feature.list.mapper.CatalogueStateMapper
import com.jacekpietras.zoo.catalogue.feature.list.mapper.DivisionMapper
import com.jacekpietras.zoo.catalogue.feature.list.model.AnimalDivision
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueState
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueViewState
import com.jacekpietras.zoo.catalogue.feature.list.router.CatalogueRouter
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.dispatcher.onMain
import com.jacekpietras.zoo.domain.interactor.LoadAnimalsUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveFilteredAnimalsUseCase
import com.jacekpietras.zoo.domain.model.AnimalFilter
import com.jacekpietras.zoo.domain.model.AnimalId
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class CatalogueViewModel(
    regionId: String?,
    private val observeFilteredAnimalsUseCase: ObserveFilteredAnimalsUseCase,
    private val stateMapper: CatalogueStateMapper,
    private val divisionMapper: DivisionMapper,
    loadAnimalsUseCase: LoadAnimalsUseCase,
) : ViewModel() {

    private val state = NullSafeMutableLiveData(CatalogueState())
    var viewState: Flow<CatalogueViewState> = state.map(stateMapper::from).asFlow()

    private val filterFlow = MutableStateFlow(AnimalFilter(regionId = regionId))

    init {
        launchInBackground {
            launch { loadAnimalsUseCase.run() }

            filterFlow
                .onEach { onMain { state.reduce { copy(filter = it) } } }
                .flatMapLatest { observeFilteredAnimalsUseCase.run(it) }
                .onEach { onMain { state.reduce { copy(animalList = it) } } }
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
        filterFlow.value = filterFlow.value.copy(query = null)
    }

    fun onSearch(query: String) {
        filterFlow.value = filterFlow.value.copy(query = query)
    }

    fun onBackClicked(router: CatalogueRouter) {
        router.goBack()
    }

    fun onCloseClicked() {
        filterFlow.value = filterFlow.value.copy(regionId = null)
    }
}