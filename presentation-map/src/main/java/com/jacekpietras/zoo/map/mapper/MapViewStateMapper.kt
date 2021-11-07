package com.jacekpietras.zoo.map.mapper

import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PathD
import com.jacekpietras.mapview.model.PolygonD
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.model.MapState
import com.jacekpietras.zoo.map.model.MapViewState
import com.jacekpietras.zoo.map.model.MapVolatileState
import com.jacekpietras.zoo.map.model.MapVolatileViewState
import timber.log.Timber

internal class MapViewStateMapper {

    fun from(state: MapVolatileState): MapVolatileViewState = with(state) {
        Timber.e("dupa update volatile")
        MapVolatileViewState(
            compass = compass,
            userPosition = userPosition,
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
            snappedPoint = snappedPoint,
            shortestPath = shortestPath,
        )
    }

    fun from(state: MapState): MapViewState = with(state) {
        Timber.e("dupa update whole map")
        MapViewState(
            worldBounds = worldBounds,
            mapData = flatListOf(
                fromPaths(roads, roadPaint),
                fromPaths(technicalRoute, technicalPaint),
                fromPaths(lines, linesPaint),
                fromPaths(takenRoute, takenRoutePaint),
                fromPolygons(buildings, buildingPaint),
                fromPolygons(aviary, aviaryPaint),
            ),
            terminalPoints = terminalPoints,
        )
    }

    private fun <T> flatListOf(vararg lists: List<T>): List<T> = listOf(*lists).flatten()

    private fun <T> fromPolygons(
        polygons: List<PolygonEntity>,
        paint: MapPaint
    ): List<MapItem<T>> =
        polygons.map { polygon ->
            MapItem(
                PolygonD(polygon.vertices),
                paint
            )
        }

    private fun <T>fromPaths(paths: List<PathEntity>, paint: MapPaint): List<MapItem<T>> =
        paths.map { polygon ->
            MapItem<T>(
                PathD(polygon.vertices),
                paint
            )
        }
}