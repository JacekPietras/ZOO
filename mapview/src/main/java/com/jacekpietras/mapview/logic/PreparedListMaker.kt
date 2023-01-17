package com.jacekpietras.mapview.logic

import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PaintHolder
import com.jacekpietras.mapview.ui.PaintBaker
import com.jacekpietras.mapview.utils.pointsToFloatArray

internal class PreparedListMaker<T>(
    private val paintBaker: PaintBaker<T>,
) {

    private val innerPaints = mutableMapOf<MapPaint, PaintHolder<T>>()
    private val borderPaints = mutableMapOf<MapPaint, PaintHolder<T>?>()

    fun toPreparedItems(list: List<MapItem>): List<PreparedItem<T>> =
        list.map { item ->
            when (item) {
                is MapItem.MapColoredItem -> {
                    val inner = innerPaints[item.paint]
                        ?: paintBaker.bakeCanvasPaint(item.paint)
                            .also { innerPaints[item.paint] = it }
                    val border = borderPaints[item.paint]
                        ?: paintBaker.bakeBorderCanvasPaint(item.paint)
                            .also { borderPaints[item.paint] = it }

                    when (item) {
                        is MapItem.MapColoredItem.PathMapItem -> PreparedItem.PreparedColoredItem.PreparedPathItem(
                            pointsToFloatArray(item.path.vertices),
                            inner,
                            border,
                            item.minZoom,
                        )
                        is MapItem.MapColoredItem.PolygonMapItem -> PreparedItem.PreparedColoredItem.PreparedPolygonItem(
                            pointsToFloatArray(item.polygon.vertices),
                            inner,
                            border,
                            item.minZoom,
                        )
                        is MapItem.MapColoredItem.CircleMapItem -> PreparedItem.PreparedColoredItem.PreparedCircleItem(
                            item.point.toFloatArray(),
                            item.radius,
                            inner,
                            border,
                            item.minZoom,
                        )
                    }
                }
                is MapItem.IconMapItem -> PreparedItem.PreparedIconItem(
                    item.point.toFloatArray(),
                    item.icon,
                    item.minZoom,
                    pivot = item.pivot,
                )
                is MapItem.BitmapMapItem -> PreparedItem.PreparedBitmapItem(
                    item.point.toFloatArray(),
                    item.bitmap,
                    item.minZoom,
                    pivot = item.pivot,
                )
            }
        }
}