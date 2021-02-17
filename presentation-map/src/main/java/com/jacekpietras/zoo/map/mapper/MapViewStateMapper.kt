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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow

internal class MapViewStateMapper {

    fun from(state: MapState, effect: Channel<MapEffect>): MapViewState {
        val buildings: Flow<List<MapItem>> = combine(
            state.buildings,
            state.buildingPaint,
            state.aviary,
            state.aviaryPaint,
        ) { building, buildingPaint, aviary, aviaryPaint ->
            fromPolygons(aviary, aviaryPaint) +
                    fromPolygons(building, buildingPaint)
        }

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

        val complex = combine(
            buildings,
            technical,
            roads,
            lines,
            taken,
        ) { a, b, c, d, e -> a + b + c + d + e }

        return MapViewState(
            currentRegionIds = state.regionsInUserPosition.map(::fromRegionId),
            worldBounds = state.worldBounds.map(::fromWorldSpace),
            mapData = complex,
            userPosition = state.userPosition.map(::fromPosition),
            compass = state.compass.map(::fromCompass),
            effect = effect.receiveAsFlow()
        )
    }

    private fun fromCompass(compass: Float): Float =
        compass

    private fun fromPosition(position: PointD): PointD =
        position

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
}