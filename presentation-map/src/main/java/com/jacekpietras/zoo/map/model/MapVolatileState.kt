package com.jacekpietras.zoo.map.model

import android.graphics.Color
import com.jacekpietras.core.PointD
import com.jacekpietras.mapview.model.MapColor
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.map.R

internal data class MapVolatileState(
    val compass: Float = 0f,
    val userPosition: PointD = PointD(),
    val snappedPoint: PointD? = null,
    val takenRoute: List<MapItemEntity.PathEntity> = emptyList(),
    val shortestPath: List<PointD> = emptyList(),
) {

    companion object {

        val shortestPathPaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Hard(Color.BLUE),
            width = MapDimension.Static.Screen(2),
        )
        val snappedPointPaint: MapPaint = MapPaint.Fill(
            fillColor = MapColor.Hard(Color.BLUE),
        )
        val userPositionPaint: MapPaint = MapPaint.Fill(
            fillColor = MapColor.Attribute(com.jacekpietras.mapview.R.attr.colorPrimary),
        )
        val takenRoutePaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Attribute(R.attr.colorMapTaken),
            width = MapDimension.Static.Screen(0.5),
        )
    }
}
