package com.jacekpietras.zoo.catalogue.feature.animal.viewmodel

import androidx.lifecycle.*
import com.jacekpietras.zoo.catalogue.feature.animal.mapper.AnimalMapper
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalState
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.router.AnimalRouter
import com.jacekpietras.zoo.core.dispatcher.dispatcherProvider
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.dispatcher.onMain
import com.jacekpietras.zoo.core.extensions.reduceOnMain
import com.jacekpietras.zoo.domain.feature.favorites.interactor.ObserveAnimalFavoritesUseCase
import com.jacekpietras.zoo.domain.feature.favorites.interactor.SetAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.AddToCurrentPlanUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.RemoveFromCurrentPlanUseCase
import com.jacekpietras.zoo.domain.interactor.*
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.model.RegionId
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus

internal class AnimalViewModel(
    animalId: AnimalId,
    mapper: AnimalMapper = AnimalMapper(),
    getAnimalUseCase: GetAnimalUseCase,
    isAnimalSeenUseCase: IsAnimalSeenUseCase,
    observeAnimalFavoritesUseCase: ObserveAnimalFavoritesUseCase,
    private val setAnimalFavoriteUseCase: SetAnimalFavoriteUseCase,
    private val addToCurrentPlanUseCase: AddToCurrentPlanUseCase,
    private val removeFromCurrentPlanUseCase: RemoveFromCurrentPlanUseCase,

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
            }.launchIn(viewModelScope + dispatcherProvider.default)

            observeAnimalFavoritesUseCase.run()
                .onEach { favorites ->
                    val isFavorite = favorites.contains(animalId)
                    state.reduceOnMain {
                        copy(isFavorite = isFavorite)
                    }
                }
                .launchIn(viewModelScope + dispatcherProvider.default)

            val isSeen = isAnimalSeenUseCase.run(animalId)
            val positions = getAnimalPositionUseCase.run(animalId)

            state.reduceOnMain {
                copy(
                    isSeen = isSeen,
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

    fun onNavClicked(router: AnimalRouter, regionId: RegionId? = null) {
        router.navigateToMap(currentState.animal.id, regionId)
    }

    fun onFavoriteClicked() {
        val isFavorite = (currentState.isFavorite ?: false).not()
        val animalId = currentState.animalId
        // fixme multiple regions! we want only one of them
        val regionId = currentState.animal.regionInZoo.first()

        launchInBackground {
            state.reduceOnMain { copy(isFavorite = isFavorite) }
            setAnimalFavoriteUseCase.run(
                animalId = animalId,
                isFavorite = isFavorite,
            )
            if (isFavorite) {
                addToCurrentPlanUseCase.run(animalId)
            } else {
                removeFromCurrentPlanUseCase.run(animalId)
            }
        }
    }
}
