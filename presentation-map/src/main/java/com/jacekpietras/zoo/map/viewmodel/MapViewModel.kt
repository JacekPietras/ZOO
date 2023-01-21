package com.jacekpietras.zoo.map.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.logic.MapViewLogic
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.ui.LastMapUpdate.trans
import com.jacekpietras.mapview.ui.PaintBaker
import com.jacekpietras.zoo.core.dispatcher.flowOnBackground
import com.jacekpietras.zoo.core.dispatcher.flowOnMain
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.dispatcher.onMain
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.core.theme.MapColors
import com.jacekpietras.zoo.domain.feature.animal.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalEntity
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import com.jacekpietras.zoo.domain.feature.favorites.interactor.ObserveAnimalFavoritesUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.LoadMapUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveMapObjectsUseCase
import com.jacekpietras.zoo.domain.feature.pathfinder.interactor.GetShortestPathFromUserUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.ObserveCurrentPlanPathWithOptimizationUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveArrivalAtRegionEventUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveCompassUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveOutsideWorldEventUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveUserPositionWithAccuracyUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StartCompassUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StartNavigationUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StopCompassUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.UploadHistoryUseCase
import com.jacekpietras.zoo.domain.interactor.FindNearRegionWithDistanceUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInRegionUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionsContainingPointUseCase
import com.jacekpietras.zoo.domain.interactor.LoadVisitedRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveRegionsWithAnimalsInUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveSuggestedThemeTypeUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveTakenRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveVisitedRoadsUseCase
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.map.BuildConfig
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.extensions.applyToMap
import com.jacekpietras.zoo.map.extensions.combine
import com.jacekpietras.zoo.map.extensions.combineWithIgnoredFlow
import com.jacekpietras.zoo.map.extensions.reduce
import com.jacekpietras.zoo.map.extensions.stateFlowOf
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.model.BitmapLibrary
import com.jacekpietras.zoo.map.model.MapAction
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.model.MapEffect.ShowToast
import com.jacekpietras.zoo.map.model.MapState
import com.jacekpietras.zoo.map.model.MapToolbarMode.AroundYouMapActionMode
import com.jacekpietras.zoo.map.model.MapToolbarMode.NavigableMapActionMode
import com.jacekpietras.zoo.map.model.MapToolbarMode.SelectedAnimalMode
import com.jacekpietras.zoo.map.model.MapToolbarMode.SelectedRegionMode
import com.jacekpietras.zoo.map.model.MapViewState
import com.jacekpietras.zoo.map.model.MapVolatileState
import com.jacekpietras.zoo.map.model.MapVolatileViewState
import com.jacekpietras.zoo.map.model.MapWorldViewState
import com.jacekpietras.zoo.map.model.PlanState
import com.jacekpietras.zoo.map.router.MapRouter
import com.jacekpietras.zoo.tracking.permissions.GpsPermissionRequester
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@OptIn(FlowPreview::class)
internal class MapViewModel<T>(
    context: Context,
    animalId: String?,
    regionId: String?,
    paintBaker: PaintBaker<T>,
    private val mapper: MapViewStateMapper,

    observeCompassUseCase: ObserveCompassUseCase,
    observeSuggestedThemeTypeUseCase: ObserveSuggestedThemeTypeUseCase,
    observeCurrentPlanPathUseCase: ObserveCurrentPlanPathWithOptimizationUseCase,
    private val observeUserPositionWithAccuracyUseCase: ObserveUserPositionWithAccuracyUseCase,
    private val stopCompassUseCase: StopCompassUseCase,
    private val startCompassUseCase: StartCompassUseCase,
    private val startNavigationUseCase: StartNavigationUseCase,

    observeMapObjectsUseCase: ObserveMapObjectsUseCase,
    observeTakenRouteUseCase: ObserveTakenRouteUseCase,
    observeVisitedRoadsUseCase: ObserveVisitedRoadsUseCase,
    observeAnimalFavoritesUseCase: ObserveAnimalFavoritesUseCase,

    loadMapUseCase: LoadMapUseCase,
    loadVisitedRouteUseCase: LoadVisitedRouteUseCase,
    observeRegionsWithAnimalsInUserPositionUseCase: ObserveRegionsWithAnimalsInUserPositionUseCase,
    private val observeOutsideWorldEventUseCase: ObserveOutsideWorldEventUseCase,
    private val observeArrivalAtRegionEventUseCase: ObserveArrivalAtRegionEventUseCase,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
    private val getRegionsContainingPointUseCase: GetRegionsContainingPointUseCase,
    private val getAnimalUseCase: GetAnimalUseCase,
    private val findNearRegionWithDistance: FindNearRegionWithDistanceUseCase,
    private val getRegionUseCase: GetRegionUseCase,
    private val uploadHistoryUseCase: UploadHistoryUseCase,
    private val getShortestPathUseCase: GetShortestPathFromUserUseCase,
) : ViewModel() {

    private val _effects = MutableStateFlow<List<MapEffect>>(emptyList())
    val effects: Flow<Unit> = _effects
        .filter(List<MapEffect>::isNotEmpty)
        .map { /* Unit */ }

    private val mapLogic = MapViewLogic(
        paintBaker = paintBaker,
        setOnPointPlacedListener = ::onPointPlaced,
        onStopCentering = ::onStopCentering,
        onStartCentering = ::onStartCentering,
        coroutineScope = viewModelScope,
    )

    fun setUpdateCallback(updateCallback: (List<RenderItem<T>>) -> Unit) {
        mapLogic.invalidate = updateCallback
    }

    private val mapColors = MutableStateFlow(MapColors())
    private val bitmapLibrary = stateFlowOf { BitmapLibrary(context) }

    private val observeTakenRoute = if (BuildConfig.DEBUG) {
        observeTakenRouteUseCase.run()
    } else {
        flow { /* Unit */ }
    }

    private val observeCurrentPlanPath = observeCurrentPlanPathUseCase.run()
        .onEach { plan ->
            val distance = plan.distanceToNextStage
            val nextStageRegion = plan.nextStageRegion
            if (plan.stages.isNotEmpty() && distance != null && nextStageRegion != null) {
                launchInBackground {
                    state.reduce {
                        copy(
                            planState = PlanState(
                                distance = distance,
                                nextStageRegion = nextStageRegion,
                            ),
                        )
                    }
                }
            } else {
                state.reduce {
                    copy(
                        planState = null,
                    )
                }
            }
        }

    private val volatileState = MutableStateFlow(MapVolatileState())
    private val volatileViewState: Flow<MapVolatileViewState> = combine(
        volatileState,
        mapColors,
        observeCurrentPlanPath,
        observeVisitedRoadsUseCase.run(),
        observeTakenRoute,
        observeCompassUseCase.run(),
        mapper::from,
    )
        .debounce(50L)
        .flowOnBackground()
        .onEach(mapLogic::applyToMap)
        .flowOnMain()

    private val mapWorldViewState: Flow<MapWorldViewState> = combine(
        mapColors,
        bitmapLibrary.filterNotNull(),
        observeMapObjectsUseCase.run(),
        mapper::from,
    )
        .debounce(50L)
        .flowOnBackground()
        .onEach(mapLogic::applyToMap)
        .flowOnMain()

    private val state = MutableStateFlow(MapState())
    val viewState: Flow<MapViewState> = combine(
        state,
        observeSuggestedThemeTypeUseCase.run(),
        observeRegionsWithAnimalsInUserPositionUseCase.run(),
        observeAnimalFavoritesUseCase.run(),
        mapper::from,
    )
        .combineWithIgnoredFlow(userPositionObservation())
        .combineWithIgnoredFlow(outsideWorldEventObservation())
        .combineWithIgnoredFlow(arrivalAtRegionEventObservation())
        .combineWithIgnoredFlow(volatileViewState)
        .combineWithIgnoredFlow(mapWorldViewState)
        .flowOnBackground()

    init {
        launchInBackground {
            loadMapUseCase.run()

            animalId.toAnimalId()?.let { animalId ->
                centerAtUserPosition()
                navigationToAnimal(getAnimalUseCase.run(animalId), regionId.toRegionId())
            }

            loadVisitedRouteUseCase.run()
        }
    }

    override fun onCleared() {
        super.onCleared()
        bitmapLibrary.value?.recycle()
    }

    private fun String?.toAnimalId(): AnimalId? =
        this?.takeIf { it.isNotBlank() }
            ?.takeIf { it != "null" }
            ?.let(::AnimalId)

    private fun String?.toRegionId(): RegionId? =
        this?.takeIf { it.isNotBlank() }
            ?.takeIf { it != "null" }
            ?.let(::RegionId)

    private suspend fun navigationToAnimal(animal: AnimalEntity, regionId: RegionId?) {
        val regionIds = if (regionId != null) {
            listOf(regionId)
        } else {
            animal.regionInZoo
        }

        val (shortestPath, distance) = findNearRegionWithDistance.run { it.id in regionIds } ?: return

        state.reduce {
            copy(
                toolbarMode = SelectedAnimalMode(
                    animal = animal,
                    distance = distance,
                    regionId = regionId,
                ),
                isToolbarOpened = true,
            )
        }
        volatileState.reduce {
            copy(
                snappedPoint = shortestPath.last(),
                shortestPath = shortestPath,
            )
        }
    }

    private fun onUploadClicked() {
        try {
            uploadHistoryUseCase.run()
        } catch (ignored: UploadHistoryUseCase.UploadFailed) {
            sendEffect(ShowToast(RichText("Upload failed")))
        }
    }

    private fun onStopCentering() {
        stopCompassUseCase.run()
    }

    private fun onStartCentering() {
        startCompassUseCase.run()
    }

    private fun onPointPlaced(point: PointD) {
        volatileState.reduce { copy(snappedPoint = point) }

        launchInBackground {
            val regionsAndAnimalsJob = async {
                getRegionsContainingPointUseCase.run(point)
                    .map { region -> region to getAnimalsInRegionUseCase.run(region.id) }
            }
            val shortestPathJob = async { getShortestPathUseCase.run(point) }

            val regionsAndAnimals = regionsAndAnimalsJob.await()
            if (regionsAndAnimals.isEmpty() || regionsAndAnimals.none(::canNavigateToIt)) {
                closeToolbar()
            } else {
                val shortestPath = shortestPathJob.await()
                state.reduce {
                    copy(
                        toolbarMode = SelectedRegionMode(regionsAndAnimals),
                        isToolbarOpened = true,
                    )
                }
                volatileState.reduce { copy(shortestPath = shortestPath) }
            }
        }
    }

    private fun closeToolbar() {
        state.reduce {
            copy(isToolbarOpened = false)
        }
        volatileState.reduce {
            copy(
                snappedPoint = null,
                shortestPath = emptyList()
            )
        }
    }

    fun onLocationButtonClicked(permissionChecker: GpsPermissionRequester) {
        permissionChecker.checkPermissions(
            onDenied = { onLocationDenied() },
            onGranted = {
                startNavigationUseCase.run()
                centerAtUserPosition()
            },
        )
    }

    private fun onLocationDenied() {
        sendEffect(ShowToast(RichText(R.string.location_denied)))
    }

    fun onCameraButtonClicked(router: MapRouter) {
        router.navigateToCamera()
    }

    fun onRegionClicked(router: MapRouter, permissionChecker: GpsPermissionRequester, regionId: RegionId) {
        launchInBackground {
            val (region, _) = getRegionUseCase.run(regionId)
            if (region is Region.AnimalRegion) {
                onMain {
                    router.navigateToAnimalList(regionId)
                }
            } else {
                permissionChecker.checkPermissions(
                    onDenied = { onLocationDenied() },
                    onGranted = {
                        startNavigationUseCase.run()
                        startNavigationToRegion(region)
                    },
                )
            }
        }
    }

    fun onCloseClicked() {
        closeToolbar()
    }

    fun onBackClicked(router: MapRouter) {
        if (state.value.isToolbarOpened) {
            closeToolbar()
        } else {
            router.goBack()
        }
    }

    fun onMapActionClicked(mapAction: MapAction) {
        centerAtUserPosition()
        when (mapAction) {
            MapAction.AROUND_YOU -> {
                state.reduce {
                    copy(
                        toolbarMode = AroundYouMapActionMode(mapAction),
                        isToolbarOpened = true,
                    )
                }
            }
            MapAction.UPLOAD -> {
                onUploadClicked()
            }
            else -> {
                launchInBackground { startNavigationToNearestRegion(mapAction) }
            }
        }
    }

    private fun startNavigationToRegion(region: Region) {
        launchInBackground {
            val mapAction = when (region) {
                is Region.RestaurantRegion -> MapAction.RESTAURANT
                is Region.ExitRegion -> MapAction.EXIT
                is Region.WcRegion -> MapAction.WC
                else -> throw IllegalStateException("Don't expect navigation to $region")
            }
            val nearWithDistance = findNearRegionWithDistance.run { it == region }
            startNavigationToActionWithPath(mapAction, nearWithDistance)
        }
    }

    private suspend fun startNavigationToNearestRegion(mapAction: MapAction) {
        val nearWithDistance = when (mapAction) {
            MapAction.WC -> findNearRegionWithDistance<Region.WcRegion>()
            MapAction.RESTAURANT -> findNearRegionWithDistance<Region.RestaurantRegion>()
            MapAction.EXIT -> findNearRegionWithDistance<Region.ExitRegion>()
            else -> throw IllegalStateException("Don't expect navigation to $mapAction")
        }
        startNavigationToActionWithPath(mapAction, nearWithDistance)
    }

    private suspend fun startNavigationToActionWithPath(
        mapAction: MapAction,
        nearWithDistance: Pair<List<PointD>, Double>?,
    ) {
        onMain {
            if (nearWithDistance != null) {
                val (path, distance) = nearWithDistance
                if (path.size > 1) {
                    state.reduce {
                        copy(
                            toolbarMode = NavigableMapActionMode(
                                mapAction = mapAction,
                                path = path,
                                distance = distance,
                            ),
                            isToolbarOpened = true,
                        )
                    }
                    volatileState.reduce {
                        copy(
                            snappedPoint = path.last(),
                            shortestPath = path,
                        )
                    }
                } else {
                    closeToolbarOnUnAccessibleMapAction(mapAction)
                }
            } else {
                closeToolbarOnUnAccessibleMapAction(mapAction)
            }
        }
    }

    private fun closeToolbarOnUnAccessibleMapAction(mapAction: MapAction) {
        state.reduce {
            copy(
                isToolbarOpened = false,
            )
        }
        sendEffect(ShowToast(RichText.Res(R.string.cannot_find_near, RichText(mapAction.title))))
    }

    private suspend inline fun <reified T> findNearRegionWithDistance(): Pair<List<PointD>, Double>? =
        findNearRegionWithDistance.run { it is T }

    fun onAnimalClicked(router: MapRouter, animalId: AnimalId) {
        router.navigateToAnimal(animalId)
    }

    fun onStopEvent() {
        stopCompassUseCase.run()
    }

    fun consumeEffect(): MapEffect {
        val removed = _effects.value.first()
        _effects.value = _effects.value.drop(1)
        return removed
    }

    private fun sendEffect(effect: MapEffect) {
        _effects.value = _effects.value + effect
    }

    private fun userPositionObservation() =
        observeUserPositionWithAccuracyUseCase.run()
            .onEach {
                val point = PointD(it.lon, it.lat)
                volatileState.reduce { copy(userPosition = point, userPositionAccuracy = it.accuracy) }
                state.reduce { copy(userPosition = point) }
                with(state.value) {
                    if (isToolbarOpened) {
                        when (toolbarMode) {
                            is NavigableMapActionMode -> startNavigationToNearestRegion(toolbarMode.mapAction)
                            is SelectedAnimalMode -> navigationToAnimal(toolbarMode.animal, toolbarMode.regionId)
                            else -> Unit
                        }
                    }
                }
            }
            .flowOnBackground()

    private fun outsideWorldEventObservation() =
        observeOutsideWorldEventUseCase.run()
            .onEach {
                when (state.value.toolbarMode) {
                    is NavigableMapActionMode,
                    is AroundYouMapActionMode,
                    is SelectedAnimalMode -> state.reduce {
                        copy(
                            toolbarMode = null,
                            isToolbarOpened = false,
                        )
                    }
                    else -> Unit
                }
                volatileState.reduce { copy(userPosition = PointD()) }
                state.reduce { copy(userPosition = PointD()) }
                sendEffect(ShowToast(RichText(R.string.outside_world_warning)))
            }

    private fun arrivalAtRegionEventObservation() =
        observeArrivalAtRegionEventUseCase.run()
            .onEach { region ->
                val regionsAndAnimals = listOf(region to getAnimalsInRegionUseCase.run(region.id))
                state.reduce {
                    copy(
                        toolbarMode = SelectedRegionMode(regionsAndAnimals),
                        isToolbarOpened = true,
                    )
                }
            }
            .flowOnBackground()

    fun fillColors(colors: MapColors) {
        mapLogic.invalidate?.invoke(emptyList())
        mapColors.value = colors
    }

    private fun centerAtUserPosition() {
        mapLogic.centerAtUserPosition()
    }

    fun onSizeChanged(width: Int, height: Int) {
        mapLogic.onSizeChanged(width, height)
    }

    fun onClick(x: Float, y: Float) {
        mapLogic.onClick(x, y)
    }

    fun onTransform(cX: Float, cY: Float, scale: Float, rotate: Float, vX: Float, vY: Float) {
        trans = System.nanoTime()

        mapLogic.onTransform(cX, cY, scale, rotate, vX, vY)
    }

    private fun canNavigateToIt(regionWithAnimals: Pair<Region, List<AnimalEntity>>): Boolean {
        val (region, animals) = regionWithAnimals

        return when (region) {
            is Region.AnimalRegion -> animals.isNotEmpty()
            is Region.WcRegion -> true
            is Region.ExitRegion -> true
            is Region.RestaurantRegion -> true
        }
    }
}
