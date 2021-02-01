package com.jacekpietras.zoo.map.model

import android.graphics.Color
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity

internal data class MapState(
    val buildings: List<PolygonEntity> = emptyList(),
    val roads: List<PathEntity> = emptyList(),
    val takenRoute: List<PathEntity> = emptyList(),

    val buildingPaint: MapPaint = redPaint,
    val roadPaint: MapPaint = strokePaint,
    val takenRoutePaint: MapPaint = dashedPaint,
)

private val redPaint: MapPaint
    get() = MapPaint.Fill(
        fillColor = MapColor.Hard(Color.RED)
    )
private val dashedPaint: MapPaint
    get() = MapPaint.DashedStroke(
        strokeColor = MapColor.Hard(Color.GREEN),
        width = MapDimension.Screen(2),
        pattern = MapDimension.Screen(8)
    )
private val strokePaint: MapPaint
    get() = MapPaint.Stroke(
        strokeColor = MapColor.Hard(Color.GREEN),
        width = MapDimension.Screen(2),
    )

