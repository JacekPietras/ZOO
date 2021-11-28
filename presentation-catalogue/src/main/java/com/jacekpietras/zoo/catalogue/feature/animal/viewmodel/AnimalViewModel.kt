package com.jacekpietras.zoo.catalogue.feature.animal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jacekpietras.zoo.catalogue.feature.animal.mapper.AnimalMapper
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalState
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.router.AnimalRouter
import com.jacekpietras.zoo.core.dispatcher.launchInMain
import com.jacekpietras.zoo.core.dispatcher.onBackground
import com.jacekpietras.zoo.core.extensions.reduceOnMain
import com.jacekpietras.zoo.domain.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.interactor.IsAnimalSeenUseCase
import com.jacekpietras.zoo.domain.model.AnimalId

internal class AnimalViewModel(
    animalId: AnimalId,
    mapper: AnimalMapper = AnimalMapper(),
    getAnimalUseCase: GetAnimalUseCase,
    isAnimalSeenUseCase: IsAnimalSeenUseCase,
) : ViewModel() {

    private val state = MutableLiveData<AnimalState>()
    private val currentState get() = checkNotNull(state.value)
    val viewState: LiveData<AnimalViewState> = state.map(mapper::from)

    init {
        launchInMain {
            state.value = AnimalState(
                animalId = animalId,
                animal = checkNotNull(getAnimalUseCase.run(animalId)),
            )
            onBackground {
                val isSeen = isAnimalSeenUseCase.run(animalId)
                state.reduceOnMain { copy(isSeen = isSeen) }
            }
        }
    }

    fun onWikiClicked(router: AnimalRouter) {
        router.navigateToWiki(currentState.animal.wiki)
    }

    fun onWebClicked(router: AnimalRouter) {
        router.navigateToWeb(currentState.animal.web)
    }

    fun onNavClicked(router: AnimalRouter, regionId: String? = null) {
        router.navigateToMap(currentState.animal.id, regionId)
    }

    fun onWantToSee() {

    }
}
