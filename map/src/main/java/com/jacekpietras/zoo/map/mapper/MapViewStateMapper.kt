package com.jacekpietras.zoo.map.mapper

import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.model.PointD
import com.jacekpietras.zoo.domain.model.RectD
import com.jacekpietras.zoo.map.model.*
import com.jacekpietras.zoo.map.ui.MapItem
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

        val taken = combine(
            state.takenRoute,
            state.takenRoutePaint,
        ) { route, paint -> fromPaths(route, paint) }

        val complex = combine(
            buildings,
            roads,
            taken,
        ) { a, b, c -> a + b + c }

        val userPosition = state.userPosition.map(::fromPosition)
        val worldSpace = state.worldSpace.map(::fromWorldSpace)

        return MapViewState(
            worldSpace = worldSpace,
            mapData = complex,
            userPosition = userPosition,
        )
    }

    private fun fromPosition(position: PointD): PointD =
        position

    private fun fromWorldSpace(worldSpace: RectD): RectD =
        worldSpace

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