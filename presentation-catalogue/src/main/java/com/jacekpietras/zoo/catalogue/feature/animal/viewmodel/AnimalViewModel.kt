package com.jacekpietras.zoo.catalogue.feature.animal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.jacekpietras.zoo.catalogue.feature.animal.mapper.AnimalMapper
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalState
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.router.AnimalRouter
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.dispatcher.launchInMain
import com.jacekpietras.zoo.core.dispatcher.onBackground
import com.jacekpietras.zoo.core.extensions.reduceOnMain
import com.jacekpietras.zoo.domain.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.interactor.IsAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.interactor.IsAnimalSeenUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveAviaryUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveBuildingsUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveRoadsUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveWorldBoundsUseCase
import com.jacekpietras.zoo.domain.interactor.SetAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.model.AnimalId
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

internal class AnimalViewModel(
    animalId: AnimalId,
    mapper: AnimalMapper = AnimalMapper(),
    getAnimalUseCase: GetAnimalUseCase,
    isAnimalSeenUseCase: IsAnimalSeenUseCase,
    isAnimalFavoriteUseCase: IsAnimalFavoriteUseCase,
    private val setAnimalFavoriteUseCase: SetAnimalFavoriteUseCase,

    observeWorldBoundsUseCase: ObserveWorldBoundsUseCase,
    observeBuildingsUseCase: ObserveBuildingsUseCase,
    observeAviaryUseCase: ObserveAviaryUseCase,
    observeRoadsUseCase: ObserveRoadsUseCase,
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
                val isFavorite = isAnimalFavoriteUseCase.run(animalId)

                state.reduceOnMain {
                    copy(
                        isSeen = isSeen,
                        isFavorite = isFavorite,
                    )
                }
            }
        }

        combine(
            observeWorldBoundsUseCase.run(),
            observeBuildingsUseCase.run(),
            observeAviaryUseCase.run(),
            observeRoadsUseCase.run(),
        ) { worldBounds, buildings, aviary, roads ->
            state.reduceOnMain {
                copy(
                    worldBounds = worldBounds,
                    buildings = buildings,
                    aviary = aviary,
                    roads = roads,
                )
            }
        }.launchIn(viewModelScope)
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

    fun onFavoriteClicked() {
        val isFavorite = (currentState.isFavorite ?: false).not()
        val animalId = currentState.animalId

        launchInBackground {
            state.reduceOnMain { copy(isFavorite = isFavorite) }
            setAnimalFavoriteUseCase.run(
                animalId = animalId,
                isFavorite = isFavorite,
            )
        }
    }
}
