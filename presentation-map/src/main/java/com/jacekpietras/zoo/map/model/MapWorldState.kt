package com.jacekpietras.zoo.map.model

import android.graphics.Color
import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.model.MapColor
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.map.R

internal data class MapWorldState(
    val worldBounds: RectD = RectD(),
    val buildings: List<PolygonEntity> = emptyList(),
    val aviary: List<PolygonEntity> = emptyList(),
    val roads: List<PathEntity> = emptyList(),
    val lines: List<PathEntity> = emptyList(),
    val takenRoute: List<PathEntity> = emptyList(),
    val technicalRoute: List<PathEntity> = emptyList(),
    val terminalPoints: List<PointD> = emptyList(),

    val buildingPaint: MapPaint = MapPaint.FillWithBorder(
        fillColor = MapColor.Attribute(R.attr.colorMapBuilding),
        borderColor = MapColor.Attribute(R.attr.colorMapBuildingBorder),
        borderWidth = MapDimension.Static.Screen(1),
    ),
    val aviaryPaint: MapPaint = MapPaint.FillWithBorder(
        fillColor = MapColor.Attribute(R.attr.colorMapBuilding),
        borderColor = MapColor.Attribute(R.attr.colorMapBuildingBorder),
        borderWidth = MapDimension.Static.Screen(1),
    ),
    val roadPaint: MapPaint = MapPaint.StrokeWithBorder(
        strokeColor = MapColor.Attribute(R.attr.colorMapRoute),
        width = MapDimension.Dynamic.World(2.0),
        borderColor = MapColor.Attribute(R.attr.colorMapRouteBorder),
        borderWidth = MapDimension.Static.Screen(1),
    ),
    val technicalPaint: MapPaint = MapPaint.StrokeWithBorder(
        strokeColor = MapColor.Attribute(R.attr.colorMapTechnical),
        width = MapDimension.Dynamic.World(2.0),
        borderColor = MapColor.Attribute(R.attr.colorMapTechnicalBorder),
        borderWidth = MapDimension.Static.Screen(1),
    ),
    val linesPaint: MapPaint = MapPaint.Stroke(
        strokeColor = MapColor.Hard(Color.BLUE),
        width = MapDimension.Dynamic.World(0.5),
    ),
    val takenRoutePaint: MapPaint = MapPaint.Stroke(
        strokeColor = MapColor.Attribute(R.attr.colorMapTaken),
        width = MapDimension.Static.Screen(0.5),
//            pattern = MapDimension.Static.Screen(8)
    ),
    val terminalPaint: MapPaint = MapPaint.Circle(
        fillColor = MapColor.Hard(Color.RED),
        radius = MapDimension.Static.Screen(5),
    ),
)
