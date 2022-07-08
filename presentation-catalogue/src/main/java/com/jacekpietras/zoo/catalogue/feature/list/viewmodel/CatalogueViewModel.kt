package com.jacekpietras.zoo.catalogue.feature.list.viewmodel

import androidx.lifecycle.ViewModel
import com.jacekpietras.zoo.catalogue.feature.list.mapper.CatalogueStateMapper
import com.jacekpietras.zoo.catalogue.feature.list.mapper.DivisionMapper
import com.jacekpietras.zoo.catalogue.feature.list.model.AnimalDivision
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueState
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueViewState
import com.jacekpietras.zoo.catalogue.feature.list.router.CatalogueRouter
import com.jacekpietras.zoo.catalogue.utils.reduce
import com.jacekpietras.zoo.core.dispatcher.flowOnBackground
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.dispatcher.onMain
import com.jacekpietras.zoo.domain.feature.animal.interactor.LoadAnimalsUseCase
import com.jacekpietras.zoo.domain.feature.animal.interactor.ObserveFilteredAnimalsUseCase
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalFilter
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach

internal class CatalogueViewModel(
    regionId: String?,
    private val observeFilteredAnimalsUseCase: ObserveFilteredAnimalsUseCase,
    private val stateMapper: CatalogueStateMapper,
    private val divisionMapper: DivisionMapper,
    loadAnimalsUseCase: LoadAnimalsUseCase,
) : ViewModel() {

    private val filterFlow = MutableStateFlow(AnimalFilter(
        regionId = regionId
            ?.takeIf { it.isNotBlank() }
            ?.takeIf { it != "null" }
            ?.let(::RegionId)
    ))
    private val animalFlow = filterFlow
        .onEach { onMain { state.reduce { copy(filter = it) } } }
        .flatMapLatest { observeFilteredAnimalsUseCase.run(it) }

    private val state = MutableStateFlow(CatalogueState())
    var viewState: Flow<CatalogueViewState> = combine(
        state,
        animalFlow,
        stateMapper::from
    ).flowOnBackground()

    init {
        launchInBackground {
            loadAnimalsUseCase.run()
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