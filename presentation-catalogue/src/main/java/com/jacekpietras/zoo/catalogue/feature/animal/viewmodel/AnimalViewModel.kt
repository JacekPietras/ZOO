package com.jacekpietras.zoo.catalogue.feature.animal.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.ComposablePaintBaker
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.zoo.catalogue.feature.animal.mapper.AnimalMapper
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalState
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.router.AnimalRouter
import com.jacekpietras.zoo.catalogue.extensions.combine
import com.jacekpietras.zoo.catalogue.extensions.combineWithIgnoredFlow
import com.jacekpietras.zoo.catalogue.extensions.reduce
import com.jacekpietras.zoo.core.dispatcher.flowOnBackground
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.theme.MapColors
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
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveUserPositionUseCase
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

internal class AnimalViewModel(
    context: Context,
    private val animalId: AnimalId,
    private val mapper: AnimalMapper = AnimalMapper(),
    getAnimalUseCase: GetAnimalUseCase,
    isAnimalSeenUseCase: IsAnimalSeenUseCase,
    private val observeAnimalFavoritesUseCase: ObserveAnimalFavoritesUseCase,
    private val setAnimalFavoriteUseCase: SetAnimalFavoriteUseCase,
    private val addAnimalToCurrentPlanUseCase: AddAnimalToCurrentPlanUseCase,
    private val removeFromCurrentPlanUseCase: RemoveAnimalFromCurrentPlanUseCase,
    observeWorldBoundsUseCase: ObserveWorldBoundsUseCase,
    observeBuildingsUseCase: ObserveBuildingsUseCase,
    observeAviaryUseCase: ObserveAviaryUseCase,
    observeRoadsUseCase: ObserveRoadsUseCase,
    observeUserPositionUseCase: ObserveUserPositionUseCase,
    getAnimalPositionUseCase: GetAnimalPositionUseCase,
    private val getShortestPathUseCase: GetShortestPathUseCase,
) : ViewModel() {

    private val paintBaker = ComposablePaintBaker(context)

    private val state = MutableStateFlow(AnimalState(animalId = animalId))
    val viewState: Flow<AnimalViewState?> = combine(
        observeWorldBoundsUseCase.run(),
        observeBuildingsUseCase.run(),
        observeAviaryUseCase.run(),
        observeRoadsUseCase.run(),
        observeUserPositionUseCase.run().map(this::getPaths).onStart { emit(emptyList()) },
        state,
        mapper::from,
    )
        .onEach { mapLogic.updateMap(it) }
        .combineWithIgnoredFlow(animalFavoritesObservation())
        .flowOnBackground()

    private val mapLogic: MapViewLogic<ComposablePaint> = makeComposableMapLogic()

    val mapList = MutableStateFlow<List<MapViewLogic.RenderItem<ComposablePaint>>>(emptyList())

    init {
        launchInBackground {
            loadAnimal(getAnimalUseCase, animalId)

            state.reduce {
                copy(
                    isSeen = isAnimalSeenUseCase.run(animalId),
                    animalPositions = getAnimalPositionUseCase.run(animalId),
                )
            }
        }
    }

    private suspend fun loadAnimal(
        getAnimalUseCase: GetAnimalUseCase,
        animalId: AnimalId
    ) {
        val animal = getAnimalUseCase.run(animalId)
        state.reduce { copy(animal = animal) }
    }

    private suspend fun getPaths(
        position: PointD
    ): List<MapItemEntity.PathEntity> {
        return state.value.animalPositions
            .map { getShortestPathUseCase.run(position, it) }
            .map { MapItemEntity.PathEntity(it) }
    }

    fun onWikiClicked(router: AnimalRouter) {
        router.navigateToWiki(checkNotNull(state.value.animal).wiki)
    }

    fun onWebClicked(router: AnimalRouter) {
        router.navigateToWeb(checkNotNull(state.value.animal).web)
    }

    fun onNavClicked(router: AnimalRouter, regionId: RegionId? = null) {
        router.navigateToMap(checkNotNull(state.value.animal).id, regionId)
    }

    fun onFavoriteClicked() {
        val isFavorite = (state.value.isFavorite ?: false).not()
        val animalId = state.value.animalId

        launchInBackground {
            state.reduce { copy(isFavorite = isFavorite) }
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

    private fun animalFavoritesObservation() =
        observeAnimalFavoritesUseCase.run()
            .onEach { favorites ->
                val isFavorite = favorites.contains(animalId)
                state.reduce { copy(isFavorite = isFavorite) }
            }

    fun fillColors(colors: MapColors) {
        mapList.value = emptyList()
        mapper.setColors(colors)
    }

    fun onSizeChanged(width: Int, height: Int) {
        mapLogic.onSizeChanged(width, height)
    }

    private fun makeComposableMapLogic() = MapViewLogic(
        invalidate = { mapList.value = it },
        paintBaker = paintBaker
    )

    private fun MapViewLogic<ComposablePaint>.updateMap(viewState: AnimalViewState?) {
        if (viewState == null) return

        worldData = MapViewLogic.WorldData(
            bounds = viewState.worldBounds,
            objectList = viewState.mapData,
        )
        setRotate(-23f)
        onScale(0f, 0f, Float.MAX_VALUE)
    }
}
