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
import com.jacekpietras.zoo.core.dispatcher.onMain
import com.jacekpietras.zoo.core.extensions.reduceOnMain
import com.jacekpietras.zoo.domain.interactor.GetAnimalPositionUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.interactor.GetShortestPathUseCase
import com.jacekpietras.zoo.domain.interactor.GetUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.IsAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.interactor.IsAnimalSeenUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveAviaryUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveBuildingsUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveRoadsUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveWorldBoundsUseCase
import com.jacekpietras.zoo.domain.interactor.SetAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.MapItemEntity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
    getAnimalPositionUseCase: GetAnimalPositionUseCase,
    observeUserPositionUseCase: GetUserPositionUseCase,
    getShortestPathUseCase: GetShortestPathUseCase,
) : ViewModel() {

    private val state = MutableLiveData<AnimalState>()
    private val currentState get() = checkNotNull(state.value)
    val viewState: LiveData<AnimalViewState> = state.map(mapper::from)

    init {
        launchInBackground {
            val animal = checkNotNull(getAnimalUseCase.run(animalId))
            onMain {
                state.value = AnimalState(
                    animalId = animalId,
                    animal = animal,
                )
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

            val isSeen = isAnimalSeenUseCase.run(animalId)
            val isFavorite = isAnimalFavoriteUseCase.run(animalId)
            val positions = getAnimalPositionUseCase.run(animalId)

            state.reduceOnMain {
                copy(
                    isSeen = isSeen,
                    isFavorite = isFavorite,
                    animalPositions = positions,
                )
            }

            observeUserPositionUseCase.run()
                .onEach { position ->
                    val pathsToAnimal = currentState.animalPositions
                        .map { getShortestPathUseCase.run(position, it) }
                        .map { MapItemEntity.PathEntity(it) }

                    state.reduceOnMain { copy(pathsToAnimal = pathsToAnimal) }
                }
                .catch { throw it }
                .launchIn(viewModelScope)
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
