package com.jacekpietras.zoo.map.mapper

import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PathD
import com.jacekpietras.mapview.model.PolygonD
import com.jacekpietras.zoo.map.model.*
import com.jacekpietras.mapview.model.MapItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

internal class MapViewStateMapper {

    fun from(state: MapState): MapViewState {
        val buildings: Flow<List<MapItem>> = combine(
            state.buildings,
            state.buildingPaint,
        ) { route, paint -> fromPolygons(route, paint) }

        val roads = combine(
            state.roads,
            state.roadPaint,
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
            roads,
            lines,
            taken,
        ) { a, b, c, d -> a + b + c + d }

        return MapViewState(
            currentRegionIds = state.regionsInUserPosition.map(::fromRegionId),
            worldBounds = state.worldBounds.map(::fromWorldSpace),
            mapData = complex,
            userPosition = state.userPosition.map(::fromPosition),
        )
    }

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