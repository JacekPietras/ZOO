package com.jacekpietras.zoo.map.mapper

import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PathD
import com.jacekpietras.mapview.model.PolygonD
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.model.*

internal class MapViewStateMapper {

    fun from(state: MapState): MapViewState = with(state) {
        MapViewState(
            title = when {
                selectedAnimal != null -> Text(selectedAnimal.name)
                else -> Text(R.string.around_you)
            },
            content = when {
                selectedAnimal != null -> Text(selectedAnimal.nameLatin)
                else -> {
                    Text.Listing(regionsInUserPosition.map { Text(it) }) +
                            "\n" +
                            Text.Listing(animalsInUserPosition.map { Text(it.name) })
                }
            },
        )
    }

    fun from(state: MapVolatileState): MapVolatileViewState = with(state) {
        MapVolatileViewState(
            compass = compass,
            userPosition = userPosition,
            snappedPoint = snappedPoint,
            shortestPath = shortestPath,
        )
    }

    fun from(state: MapWorldState): MapWorldViewState = with(state) {
        MapWorldViewState(
            worldBounds = worldBounds,
            mapData = flatListOf(
                fromPaths(technicalRoute, technicalPaint),
                fromPaths(roads, roadPaint),
                fromPaths(lines, linesPaint),
                fromPaths(takenRoute, takenRoutePaint),
                fromPolygons(buildings, buildingPaint),
                fromPolygons(aviary, aviaryPaint),
            ),
            terminalPoints = terminalPoints,
        )
    }

    private fun <T> flatListOf(vararg lists: List<T>): List<T> = listOf(*lists).flatten()

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