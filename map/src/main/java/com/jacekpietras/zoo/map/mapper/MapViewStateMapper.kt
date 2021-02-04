package com.jacekpietras.zoo.map.mapper

import android.graphics.PointF
import com.jacekpietras.zoo.domain.model.LatLon
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.map.model.*
import com.jacekpietras.zoo.map.ui.MapItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine

internal class MapViewStateMapper {

    suspend fun from(state: MapState, viewState: MapViewState) {
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

        complex.collect {
            viewState.mapData.value = it
        }

        state.userPosition.collect {
            viewState.userPosition.value = fromPosition(it)
        }
    }

    private fun fromPosition(position: LatLon): LatLon =
        position

    private fun fromPolygons(
        polygons: List<PolygonEntity>,
        paint: MapPaint
    ): List<MapItem> =
        polygons.map { polygon ->
            MapItem(
                PolygonF(polygon.vertices.map {
                    PointF(it.lat.toFloat(), it.lon.toFloat())
                }),
                paint
            )
        }

    private fun fromPaths(paths: List<PathEntity>, paint: MapPaint): List<MapItem> =
        paths.map { polygon ->
            MapItem(
                PathF(polygon.vertices.map {
                    PointF(it.lat.toFloat(), it.lon.toFloat())
                }),
                paint
            )
        }
}