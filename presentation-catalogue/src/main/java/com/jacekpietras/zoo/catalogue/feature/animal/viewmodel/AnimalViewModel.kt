package com.jacekpietras.zoo.catalogue.feature.animal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.catalogue.feature.animal.mapper.AnimalMapper
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalState
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.router.AnimalRouter
import com.jacekpietras.zoo.core.dispatcher.dispatcherProvider
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.extensions.reduceOnMain
import com.jacekpietras.zoo.domain.feature.animal.interactor.GetAnimalPositionUseCase
import com.jacekpietras.zoo.domain.feature.animal.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.feature.animal.interactor.IsAnimalSeenUseCase
import com.jacekpietras.zoo.domain.feature.favorites.interactor.ObserveAnimalFavoritesUseCase
import com.jacekpietras.zoo.domain.feature.favorites.interactor.SetAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveAviaryUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveBuildingsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveRoadsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveWorldBoundsUseCase
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity
import com.jacekpietras.zoo.domain.feature.pathfinder.interactor.GetShortestPathUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.AddAnimalToCurrentPlanUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.RemoveAnimalFromCurrentPlanUseCase
import com.jacekpietras.zoo.domain.interactor.GetUserPositionUseCase
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus

internal class AnimalViewModel(
    animalId: AnimalId,
    mapper: AnimalMapper = AnimalMapper(),
    getAnimalUseCase: GetAnimalUseCase,
    isAnimalSeenUseCase: IsAnimalSeenUseCase,
    observeAnimalFavoritesUseCase: ObserveAnimalFavoritesUseCase,
    private val setAnimalFavoriteUseCase: SetAnimalFavoriteUseCase,
    private val addAnimalToCurrentPlanUseCase: AddAnimalToCurrentPlanUseCase,
    private val removeFromCurrentPlanUseCase: RemoveAnimalFromCurrentPlanUseCase,

    observeWorldBoundsUseCase: ObserveWorldBoundsUseCase,
    observeBuildingsUseCase: ObserveBuildingsUseCase,
    observeAviaryUseCase: ObserveAviaryUseCase,
    observeRoadsUseCase: ObserveRoadsUseCase,
    getAnimalPositionUseCase: GetAnimalPositionUseCase,
    observeUserPositionUseCase: GetUserPositionUseCase,
    private val getShortestPathUseCase: GetShortestPathUseCase,
) : ViewModel() {

    private val state = MutableStateFlow(AnimalState(animalId = animalId))
    private val currentState get() = state.value
    val viewState: Flow<AnimalViewState?> =
        combine(
            observeWorldBoundsUseCase.run().flowOn(dispatcherProvider.default),
            observeBuildingsUseCase.run().flowOn(dispatcherProvider.default),
            observeAviaryUseCase.run().flowOn(dispatcherProvider.default),
            observeRoadsUseCase.run().flowOn(dispatcherProvider.default),
            observeUserPositionUseCase.run().map(this::getPaths).flowOn(dispatcherProvider.default),
            state,
            mapper::from,
        )

    init {
        launchInBackground {
            val animal = getAnimalUseCase.run(animalId)
            state.reduceOnMain { copy(animal = animal) }

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
        }
    }

    private suspend fun getPaths(
        position: PointD
    ): List<MapItemEntity.PathEntity> {
        return currentState.animalPositions
            .map { getShortestPathUseCase.run(position, it) }
            .map { MapItemEntity.PathEntity(it) }
    }

    fun onWikiClicked(router: AnimalRouter) {
        router.navigateToWiki(checkNotNull(currentState.animal).wiki)
    }

    fun onWebClicked(router: AnimalRouter) {
        router.navigateToWeb(checkNotNull(currentState.animal).web)
    }

    fun onNavClicked(router: AnimalRouter, regionId: RegionId? = null) {
        router.navigateToMap(checkNotNull(currentState.animal).id, regionId)
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
            if (isFavorite) {
                addAnimalToCurrentPlanUseCase.run(animalId)
            } else {
                removeFromCurrentPlanUseCase.run(animalId)
            }
        }
    }
}

fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> = combine(
    combine(flow, flow2, flow3, ::Triple),
    combine(flow4, flow5, flow6, ::Triple),
) { t1, t2 ->
    transform(
        t1.first,
        t1.second,
        t1.third,
        t2.first,
        t2.second,
        t2.third,
    )
}

fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R
): Flow<R> = combine(
    combine(flow, flow2, flow3, ::Triple),
    combine(flow4, flow5, flow6, ::Triple),
    flow7
) { t1, t2, t3 ->
    transform(
        t1.first,
        t1.second,
        t1.third,
        t2.first,
        t2.second,
        t2.third,
        t3,
    )
}
