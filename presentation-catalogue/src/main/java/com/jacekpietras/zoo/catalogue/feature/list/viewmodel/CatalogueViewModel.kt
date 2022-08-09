package com.jacekpietras.zoo.catalogue.feature.list.viewmodel

import androidx.lifecycle.ViewModel
import com.jacekpietras.zoo.catalogue.extensions.reduce
import com.jacekpietras.zoo.catalogue.feature.list.mapper.CatalogueStateMapper
import com.jacekpietras.zoo.catalogue.feature.list.mapper.DivisionMapper
import com.jacekpietras.zoo.catalogue.feature.list.model.AnimalDivision
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueState
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueViewState
import com.jacekpietras.zoo.catalogue.feature.list.router.CatalogueRouter
import com.jacekpietras.zoo.core.dispatcher.flowOnBackground
import com.jacekpietras.zoo.core.dispatcher.onMain
import com.jacekpietras.zoo.domain.feature.animal.interactor.ObserveFilteredAnimalsUseCase
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalFilter
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

internal class CatalogueViewModel(
    regionId: String?,
    private val observeFilteredAnimalsUseCase: ObserveFilteredAnimalsUseCase,
    private val stateMapper: CatalogueStateMapper,
    private val divisionMapper: DivisionMapper,
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
        .flowOnBackground()
        .onStart { emit(emptyList()) }

    private val state = MutableStateFlow(CatalogueState())
    val viewState: Flow<CatalogueViewState> = combine(
        state,
        animalFlow,
        stateMapper::from,
    ).flowOnBackground()

    fun onAnimalClicked(animalId: String, router: CatalogueRouter) {
        router.navigateToAnimal(AnimalId(animalId))
    }

    fun onFilterClicked(clickedDivision: AnimalDivision) {
        val domainDivision = divisionMapper.from(clickedDivision)
        filterFlow.value = with(filterFlow.value) {
            if (division == domainDivision) {
                copy(division = null)
            } else {
                copy(division = domainDivision)
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