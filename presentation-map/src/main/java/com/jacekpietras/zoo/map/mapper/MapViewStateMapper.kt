package com.jacekpietras.zoo.map.mapper

import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PathD
import com.jacekpietras.mapview.model.PolygonD
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.model.MapState
import com.jacekpietras.zoo.map.model.MapViewState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

internal class MapViewStateMapper {

    fun from(state: MapState, effect: Channel<MapEffect>): MapViewState {
        val roads = combine(
            state.roads,
            state.roadPaint,
        ) { route, paint -> fromPaths(route, paint) }

        val technical = combine(
            state.technicalRoute,
            state.technicalPaint,
        ) { route, paint -> fromPaths(route, paint) }

        val lines = combine(
            state.lines,
            state.linesPaint,
        ) { route, paint -> fromPaths(route, paint) }

        val taken = combine(
            state.takenRoute,
            state.takenRoutePaint,
        ) { route, paint -> fromPaths(route, paint) }

        val buildings: Flow<List<MapItem>> = combine(
            state.buildings,
            state.buildingPaint,
        ) { building, buildingPaint ->
            fromPolygons(building, buildingPaint)
        }

        val aviary: Flow<List<MapItem>> = combine(
            state.aviary,
            state.aviaryPaint,
        ) { aviary, aviaryPaint ->
            fromPolygons(aviary, aviaryPaint)
        }

        val complex = combineSum(
            technical,
            roads,
            lines,
            buildings,
            aviary,
            taken,
        )

        return MapViewState(
            currentRegionIds = state.regionsInUserPosition.map(::fromRegionId),
            worldBounds = state.worldBounds.map(::fromWorldSpace),
            mapData = complex,
            userPosition = state.userPosition.map(::fromPoint),
            terminalPoints = state.terminalPoints.map(::fromPoints),
            compass = state.compass.map(::fromCompass),
            snappedPoint = state.snappedPoint.filterNotNull().map(::fromPoint),
            shortestPath = state.shortestPath.map(::fromPoints),
            effect = effect.receiveAsFlow()
        )
    }

    private fun fromCompass(compass: Float): Float =
        compass

    private fun fromPoint(position: PointD): PointD =
        position

    private fun fromPoints(points: List<PointD>): List<PointD> =
        points

    private fun fromWorldSpace(worldSpace: RectD): RectD =
        worldSpace

    private fun fromRegionId(regionIds: List<String>): String =
        regionIds.joinToString(separator = ", ")

    private fun fromPolygons(
        polygons: List<PolygonEntity>,
        paint: MapPaint
    ): List<MapItem> =
        polygons.map { polygon ->
            MapItem(
                PolygonD(polygon.vertices),
                paint
            )
        }

    private fun fromPaths(paths: List<PathEntity>, paint: MapPaint): List<MapItem> =
        paths.map { polygon ->
            MapItem(
                PathD(polygon.vertices),
                paint
            )
        }

    private fun <T> combineSum(vararg flow: Flow<List<T>>): Flow<List<T>> =
        combine(flow.toList()) { a -> a.toList().flatten() }
}