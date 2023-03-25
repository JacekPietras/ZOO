package com.jacekpietras.zoo.catalogue.feature.animal.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.logic.MapViewLogic
import com.jacekpietras.mapview.logic.WorldData
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.ui.compose.MapRenderer
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.catalogue.extensions.combine
import com.jacekpietras.zoo.catalogue.extensions.combineWithIgnoredFlow
import com.jacekpietras.zoo.catalogue.feature.animal.mapper.AnimalMapper
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalEffect
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalEffect.ShowToast
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.model.MutableAnimalState
import com.jacekpietras.zoo.catalogue.feature.animal.router.AnimalRouter
import com.jacekpietras.zoo.core.dispatcher.flowOnBackground
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.core.theme.MapColors
import com.jacekpietras.zoo.domain.feature.animal.interactor.GetAnimalPositionUseCase
import com.jacekpietras.zoo.domain.feature.animal.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.feature.animal.interactor.IsAnimalSeenUseCase
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
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
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StartNavigationUseCase
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.tracking.permissions.GpsPermissionRequester
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

internal class AnimalViewModel(
    context: Context,
    mapRenderer: MapRenderer,
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
    private val startNavigationUseCase: StartNavigationUseCase,
) : ViewModel() {

    private val state = MutableAnimalState(
        animalId = animalId,
        animal = null,
        isSeen = null,
        isFavorite = null,
        animalPositions = emptyList(),
    )
    val viewState: Flow<AnimalViewState?> = combine(
        observeWorldBoundsUseCase.run(),
        observeBuildingsUseCase.run(),
        observeAviaryUseCase.run(),
        observeRoadsUseCase.run(),
        observeUserPositionUseCase.run().map(this::getPaths).onStart { emit(emptyList()) },
        state.asStateFlow(),
        mapper::from,
    )
        .onEach { mapLogic.updateMap(it) }
        .combineWithIgnoredFlow(animalFavoritesObservation())
        .flowOnBackground()

    private val _effects = MutableStateFlow<List<AnimalEffect>>(emptyList())
    val effects: Flow<Unit> = _effects
        .filter { it.isNotEmpty() }
        .map { /* Unit */ }

    private val mapLogic = MapViewLogic<Any>(
        context = context,
        mapRenderer = mapRenderer,
        coroutineScope = viewModelScope,
    )

    fun setUpdateCallback(updateCallback: (List<RenderItem<Any>>) -> Unit) {
        mapLogic.invalidate = updateCallback
    }

    init {
        launchInBackground {
            loadAnimal(getAnimalUseCase, animalId)

            state.isSeen = isAnimalSeenUseCase.run(animalId)
            state.animalPositions = getAnimalPositionUseCase.run(animalId)
        }
    }

    private suspend fun loadAnimal(
        getAnimalUseCase: GetAnimalUseCase,
        animalId: AnimalId
    ) {
        state.animal = getAnimalUseCase.run(animalId)
    }

    private suspend fun getPaths(
        position: PointD
    ): List<MapItemEntity.PathEntity> {
        return state.animalPositions
            .map { getShortestPathUseCase.run(position, it) }
            .map { MapItemEntity.PathEntity(it) }
    }

    fun onWikiClicked(router: AnimalRouter) {
        router.navigateToWiki(checkNotNull(state.animal).wiki)
    }

    fun onWebClicked(router: AnimalRouter) {
        router.navigateToWeb(checkNotNull(state.animal).web)
    }

    fun onNavClicked(router: AnimalRouter, regionId: RegionId? = null, permissionChecker: GpsPermissionRequester) {
        permissionChecker.checkPermissions(
            onDenied = { onLocationDenied() },
            onGranted = {
                startNavigationUseCase.run()
                router.navigateToMap(checkNotNull(state.animal).id, regionId)
            },
        )
    }

    fun onFavoriteClicked() {
        val isFavorite = (state.isFavorite ?: false).not()
        val animalId = state.animalId

        launchInBackground {
            state.isFavorite = isFavorite
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
                state.isFavorite = favorites.contains(animalId)
            }

    fun fillColors(colors: MapColors) {
        mapLogic.redraw()
        mapper.setColors(colors)
    }

    fun onSizeChanged(width: Int, height: Int) {
        mapLogic.onSizeChanged(width, height)
    }

    fun consumeEffect(): AnimalEffect {
        val removed = _effects.value.first()
        _effects.value = _effects.value.drop(1)
        return removed
    }

    private fun sendEffect(effect: AnimalEffect) {
        _effects.value = _effects.value + effect
    }

    private fun onLocationDenied() {
        sendEffect(ShowToast(RichText(R.string.location_denied)))
    }

    private fun MapViewLogic<Any>.updateMap(viewState: AnimalViewState?) {
        if (viewState == null) return

        worldData = WorldData(
            bounds = viewState.worldBounds,
            objectList = viewState.mapData,
        )
        setRotate(-23f)
        onScale(0f, 0f, Float.MAX_VALUE)
    }
}
