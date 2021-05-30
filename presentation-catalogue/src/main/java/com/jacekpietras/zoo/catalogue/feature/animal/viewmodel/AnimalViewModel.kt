package com.jacekpietras.zoo.catalogue.feature.animal.viewmodel

import androidx.lifecycle.*
import com.jacekpietras.zoo.catalogue.feature.animal.mapper.AnimalMapper
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalState
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.core.dispatcher.DefaultDispatcherProvider
import com.jacekpietras.zoo.core.dispatcher.DispatcherProvider
import com.jacekpietras.zoo.domain.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.model.AnimalId
import kotlinx.coroutines.launch

internal class AnimalViewModel(
    animalId: AnimalId,
    mapper: AnimalMapper = AnimalMapper(),
    getAnimalUseCase: GetAnimalUseCase,
    dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
) : ViewModel() {

    private val state: MutableLiveData<AnimalState> =
        MutableLiveData()
    val viewState: LiveData<AnimalViewState> = state.map(mapper::from)

    init {
        viewModelScope.launch(dispatcherProvider.main) {
            state.value = AnimalState(
                animalId = animalId,
                animal = getAnimalUseCase.run(animalId),
            )
        }
    }
}