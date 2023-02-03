package com.jacekpietras.mapview.logic

import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PaintHolder
import com.jacekpietras.mapview.ui.PaintBaker
import com.jacekpietras.mapview.utils.pointsToDoubleArray

internal class PreparedListMaker<T>(
    private val paintBaker: PaintBaker<T>,
) {

    private val paints = mutableMapOf<MapPaint, Pair<PaintHolder<T>, PaintHolder<T>?>>()

    fun toPreparedItems(list: List<MapItem>): List<PreparedItem<T>> =
        list.map { item ->
            when (item) {
                is MapItem.MapColoredItem -> {
                    val (inner, border) = paints[item.paint]
                        ?: paintBaker.bakeCanvasPaint(item.paint)
                            .also { paints[item.paint] = it }

                    when (item) {
                        is MapItem.MapColoredItem.PathMapItem -> PreparedItem.PreparedColoredItem.PreparedPathItem(
                            pointsToDoubleArray(item.path.vertices),
                            inner,
                            border,
                            item.minZoom,
                        )
                        is MapItem.MapColoredItem.PolygonMapItem -> {
                            if (item.is3DBlock) {
                                PreparedItem.PreparedColoredItem.PreparedPolygonItem.Block(
                                    shape = pointsToDoubleArray(item.polygon.vertices),
                                    paintHolder = inner,
                                    minZoom = item.minZoom,
                                    cacheTranslated = FloatArray(item.polygon.vertices.size * 2),
                                    cacheRoofTranslated = FloatArray(item.polygon.vertices.size * 2),
                                )
                            } else {
                                PreparedItem.PreparedColoredItem.PreparedPolygonItem.Plain(
                                    shape = pointsToDoubleArray(item.polygon.vertices),
                                    paintHolder = inner,
                                    outerPaintHolder = border,
                                    minZoom = item.minZoom,
                                    cacheTranslated = FloatArray(item.polygon.vertices.size * 2),
                                )
                            }
                        }
                        is MapItem.MapColoredItem.CircleMapItem -> PreparedItem.PreparedColoredItem.PreparedCircleItem(
                            item.point,
                            item.radius,
                            inner,
                            border,
                            item.minZoom,
                        )
                    }
                }
                is MapItem.BitmapMapItem -> PreparedItem.PreparedBitmapItem(
                    item.point,
                    item.bitmap,
                    item.minZoom,
                    pivot = item.pivot,
                )
            }
        }

    fun clear() {
        paints.clear()
    }
}