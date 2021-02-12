package com.jacekpietras.zoo.map.model

import android.graphics.Color
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.model.PointD
import com.jacekpietras.zoo.domain.model.RectD
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal data class MapState(
    val regionsInUserPosition: Flow<List<String>> = flowOf(emptyList()),

    val buildings: Flow<List<PolygonEntity>> = flowOf(emptyList()),
    val roads: Flow<List<PathEntity>> = flowOf(emptyList()),
    val takenRoute: Flow<List<PathEntity>> = flowOf(emptyList()),

    val buildingPaint: Flow<MapPaint> = flowOf(redPaint),
    val roadPaint: Flow<MapPaint> = flowOf(strokePaint),
    val takenRoutePaint: Flow<MapPaint> = flowOf(dashedPaint),

    val userPosition: Flow<PointD>,

    val worldBounds: Flow<RectD>,
)

private val redPaint: MapPaint
    get() = MapPaint.Fill(
        fillColor = MapColor.Hard(Color.RED)
    )
private val dashedPaint: MapPaint
    get() = MapPaint.DashedStroke(
        strokeColor = MapColor.Hard(Color.YELLOW),
        width = MapDimension.Screen(2),
        pattern = MapDimension.Screen(8)
    )
private val strokePaint: MapPaint
    get() = MapPaint.Stroke(
        strokeColor = MapColor.Hard(Color.GREEN),
        width = MapDimension.Screen(2),
    )

