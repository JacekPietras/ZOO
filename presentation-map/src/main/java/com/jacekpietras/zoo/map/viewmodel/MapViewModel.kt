package com.jacekpietras.zoo.map.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.ComposablePaintBaker
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.mapview.ui.MapViewLogic.RenderItem
import com.jacekpietras.zoo.core.dispatcher.flowOnBackground
import com.jacekpietras.zoo.core.dispatcher.flowOnMain
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.dispatcher.onMain
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.core.theme.MapColors
import com.jacekpietras.zoo.domain.feature.animal.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.LoadMapUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveAviaryUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveBuildingsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveMapLinesUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveRoadsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveTechnicalRoadsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveWorldBoundsUseCase
import com.jacekpietras.zoo.domain.feature.pathfinder.interactor.GetShortestPathFromUserUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.ObserveCurrentPlanPathWithOptimizationUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveArrivalAtRegionEventUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveCompassUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveOutsideWorldEventUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveUserPositionUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StartCompassUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StartNavigationUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StopCompassUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.UploadHistoryUseCase
import com.jacekpietras.zoo.domain.interactor.FindNearRegionWithDistanceUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInRegionUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionsContainingPointUseCase
import com.jacekpietras.zoo.domain.interactor.LoadVisitedRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveOldTakenRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveRegionCentersUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveRegionsWithAnimalsInUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveSuggestedThemeTypeUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveTakenRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveVisitedRoadsUseCase
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.map.BuildConfig
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.extensions.applyToMap
import com.jacekpietras.zoo.map.extensions.combine
import com.jacekpietras.zoo.map.extensions.combineWithIgnoredFlow
import com.jacekpietras.zoo.map.extensions.reduce
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.model.MapAction
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.model.MapEffect.ShowToast
import com.jacekpietras.zoo.map.model.MapState
import com.jacekpietras.zoo.map.model.MapToolbarMode
import com.jacekpietras.zoo.map.model.MapViewState
import com.jacekpietras.zoo.map.model.MapVolatileState
import com.jacekpietras.zoo.map.model.MapVolatileViewState
import com.jacekpietras.zoo.map.model.MapWorldViewState
import com.jacekpietras.zoo.map.router.MapRouter
import com.jacekpietras.zoo.map.service.TrackingServiceStarter
import com.jacekpietras.zoo.tracking.permissions.GpsPermissionRequester
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class MapViewModel(
    context: Context,
    animalId: String?,
    regionId: String?,
    private val mapper: MapViewStateMapper,

    observeCompassUseCase: ObserveCompassUseCase,
    observeSuggestedThemeTypeUseCase: ObserveSuggestedThemeTypeUseCase,
    observeCurrentPlanPathUseCase: ObserveCurrentPlanPathWithOptimizationUseCase,
    private val observeUserPositionUseCase: ObserveUserPositionUseCase,
    private val stopCompassUseCase: StopCompassUseCase,
    private val startCompassUseCase: StartCompassUseCase,
    private val startNavigationUseCase: StartNavigationUseCase,
    private val trackingServiceStarter: TrackingServiceStarter,

    observeWorldBoundsUseCase: ObserveWorldBoundsUseCase,
    observeBuildingsUseCase: ObserveBuildingsUseCase,
    observeAviaryUseCase: ObserveAviaryUseCase,
    observeRoadsUseCase: ObserveRoadsUseCase,
    observeTechnicalRoadsUseCase: ObserveTechnicalRoadsUseCase,
    observeTakenRouteUseCase: ObserveTakenRouteUseCase,
    observeOldTakenRouteUseCase: ObserveOldTakenRouteUseCase,
    observeRegionCentersUseCase: ObserveRegionCentersUseCase,
    observeMapLinesUseCase: ObserveMapLinesUseCase,
    observeVisitedRoadsUseCase: ObserveVisitedRoadsUseCase,

    loadMapUseCase: LoadMapUseCase,
    loadVisitedRouteUseCase: LoadVisitedRouteUseCase,
    observeRegionsWithAnimalsInUserPositionUseCase: ObserveRegionsWithAnimalsInUserPositionUseCase,
    private val observeOutsideWorldEventUseCase: ObserveOutsideWorldEventUseCase,
    private val observeArrivalAtRegionEventUseCase: ObserveArrivalAtRegionEventUseCase,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
    private val getRegionsContainingPointUseCase: GetRegionsContainingPointUseCase,
    private val getAnimalUseCase: GetAnimalUseCase,
    private val findNearRegionWithDistance: FindNearRegionWithDistanceUseCase,
    private val uploadHistoryUseCase: UploadHistoryUseCase,
    private val getShortestPathUseCase: GetShortestPathFromUserUseCase,
) : ViewModel() {

    private val paintBaker = ComposablePaintBaker(context)

    private val _effects = MutableStateFlow<List<MapEffect>>(emptyList())
    val effects: Flow<Unit> = _effects
        .filter { it.isNotEmpty() }
        .map { }

    private val mapLogic: MapViewLogic<ComposablePaint> = makeComposableMapLogic {
        mapList.value = it
    }

    val mapList = MutableStateFlow<List<RenderItem<ComposablePaint>>>(emptyList())

    private val mapColors = MutableStateFlow(MapColors())

    private val volatileState = MutableStateFlow(MapVolatileState())
    private val volatileViewState: Flow<MapVolatileViewState> = combine(
        volatileState,
        mapColors,
        observeCurrentPlanPathUseCase.run(),
        observeVisitedRoadsUseCase.run(),
        observeTakenRouteUseCase.run(),
        observeCompassUseCase.run(),
        mapper::from,
    )
        .flowOnBackground()
        .onEach(mapLogic::applyToMap)
        .flowOnMain()

    private val mapWorldViewState: Flow<MapWorldViewState> = combine(
        mapColors,
        observeWorldBoundsUseCase.run(),
        observeBuildingsUseCase.run(),
        observeAviaryUseCase.run(),
        observeRoadsUseCase.run(),
        observeMapLinesUseCase.run(),
        observeTechnicalRoadsUseCase.run(),
        observeOldTakenRouteUseCase.run(),
        observeRegionCentersUseCase.run(),
        mapper::from,
    )
        .flowOnBackground()
        .onEach(mapLogic::applyToMap)
        .flowOnMain()

    private val state = MutableStateFlow(MapState())
    val viewState: Flow<MapViewState> = combine(
        state,
        observeSuggestedThemeTypeUseCase.run(),
        observeRegionsWithAnimalsInUserPositionUseCase.run(),
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
                toolbarMode = MapToolbarMode.SelectedAnimalMode(
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
        launchInBackground {
            val regionsAndAnimals = getRegionsContainingPointUseCase.run(point)
                .map { region -> region to getAnimalsInRegionUseCase.run(region.id) }
                .filter { (_, animals) -> animals.isNotEmpty() }

            if (regionsAndAnimals.isEmpty()) {
                state.reduce {
                    copy(isToolbarOpened = false)
                }
                return@launchInBackground
            } else {
                state.reduce {
                    copy(
                        toolbarMode = MapToolbarMode.SelectedRegionMode(regionsAndAnimals),
                        isToolbarOpened = true,
                    )
                }
            }
        }
        volatileState.reduce { copy(snappedPoint = point) }
        launchInBackground {
            val shortestPath = getShortestPathUseCase.run(point)
            volatileState.reduce { copy(shortestPath = shortestPath) }
        }
    }

    fun onLocationButtonClicked(permissionChecker: GpsPermissionRequester) {
        startNavigationUseCase.run()
        permissionChecker.checkPermissions(
            onDenied = { onLocationDenied() },
            onGranted = {
                trackingServiceStarter.run()
                centerAtUserPosition()
            },
        )
    }

    private fun onLocationDenied() {
        if (BuildConfig.DEBUG) {
            sendEffect(ShowToast(RichText(R.string.location_denied)))
        }
    }

    fun onCameraButtonClicked(router: MapRouter) {
        router.navigateToCamera()
    }

    fun onRegionClicked(router: MapRouter, regionId: RegionId) {
        router.navigateToAnimalList(regionId)
    }

    fun onCloseClicked() {
        state.reduce {
            copy(
                isToolbarOpened = false,
            )
        }
        volatileState.reduce {
            copy(
                snappedPoint = null,
                shortestPath = emptyList(),
            )
        }
    }

    fun onBackClicked(router: MapRouter) {
        router.goBack()
    }

    fun onMapActionClicked(mapAction: MapAction) {
        centerAtUserPosition()
        val toolbarMode = when (mapAction) {
            MapAction.AROUND_YOU -> MapToolbarMode.AroundYouMapActionMode(mapAction)
            MapAction.UPLOAD -> {
                onUploadClicked()
                null
            }
            else -> MapToolbarMode.NavigableMapActionMode(mapAction)
        }
        state.reduce {
            copy(
                toolbarMode = toolbarMode,
                isToolbarOpened = toolbarMode != null,
            )
        }
        if (toolbarMode is MapToolbarMode.NavigableMapActionMode) {
            launchInBackground { startNavigationToNearestRegion(mapAction) }
        }
    }

    private suspend fun startNavigationToNearestRegion(mapAction: MapAction) {
        val nearWithDistance = when (mapAction) {
            MapAction.WC -> findNearRegionWithDistance<Region.WcRegion>()
            MapAction.RESTAURANT -> findNearRegionWithDistance<Region.RestaurantRegion>()
            MapAction.EXIT -> findNearRegionWithDistance<Region.ExitRegion>()
            else -> throw IllegalStateException("Don't expect navigation to $mapAction")
        }
        onMain {
            if (nearWithDistance != null) {
                val (path, distance) = nearWithDistance

                (state.value.toolbarMode as? MapToolbarMode.NavigableMapActionMode)?.let { currentMode ->
                    state.reduce {
                        copy(
                            toolbarMode = currentMode.copy(
                                path = path,
                                distance = distance,
                            ),
                        )
                    }
                    volatileState.reduce {
                        copy(
                            snappedPoint = path.last(),
                            shortestPath = path,
                        )
                    }
                }
            } else {
                state.reduce { copy(isToolbarOpened = false) }
                sendEffect(ShowToast(RichText.Res(R.string.cannot_find_near, RichText(mapAction.title))))
            }
        }
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
        observeUserPositionUseCase.run()
            .onEach {
                volatileState.reduce { copy(userPosition = it) }
                state.reduce { copy(userPosition = it) }
                with(state.value) {
                    if (isToolbarOpened) {
                        when (toolbarMode) {
                            is MapToolbarMode.NavigableMapActionMode -> startNavigationToNearestRegion(toolbarMode.mapAction)
                            is MapToolbarMode.SelectedAnimalMode -> navigationToAnimal(toolbarMode.animal, toolbarMode.regionId)
                            else -> Unit
                        }
                    }
                }
            }
            .flowOnBackground()

    private fun outsideWorldEventObservation() =
        observeOutsideWorldEventUseCase.run()
            .onEach {
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
                        toolbarMode = MapToolbarMode.SelectedRegionMode(regionsAndAnimals),
                        isToolbarOpened = true,
                    )
                }
            }
            .flowOnBackground()

    fun fillColors(colors: MapColors) {
        mapList.value = emptyList()
        mapColors.value = colors
    }

    private fun makeComposableMapLogic(
        invalidate: (List<RenderItem<ComposablePaint>>) -> Unit
    ): MapViewLogic<ComposablePaint> {
        return MapViewLogic(
            invalidate = invalidate,
            paintBaker = paintBaker,
            setOnPointPlacedListener = { onPointPlaced(it) },
            onStopCentering = { onStopCentering() },
            onStartCentering = { onStartCentering() },
        )
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
        mapLogic.onTransform(cX, cY, scale, rotate, vX, vY)
    }
}
