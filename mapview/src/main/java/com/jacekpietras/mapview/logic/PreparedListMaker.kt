package com.jacekpietras.mapview.logic

import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PaintHolder
import com.jacekpietras.mapview.ui.MapRenderConfig.isTriangulated
import com.jacekpietras.mapview.ui.PaintBaker
import com.jacekpietras.mapview.ui.PathBaker
import com.jacekpietras.mapview.utils.pointsToDoubleArray
import com.jacekpietras.mapview.utils.toShortArray
import earcut4j.Earcut


internal class PreparedListMaker<T>(
    private val paintBaker: PaintBaker<T>,
) {

    private val pathBaker = (paintBaker as? PathBaker)
    private val paints = mutableMapOf<MapPaint, Pair<PaintHolder<T>, PaintHolder<T>?>>()

    fun toPreparedItems(list: List<MapItem>): List<PreparedItem<T>> =
        list.map { item ->
            when (item) {
                is MapItem.MapColoredItem -> {
                    val (inner, border) = paints[item.paint]
                        ?: paintBaker.bakeCanvasPaint(item.paint)
                            .also { paints[item.paint] = it }

                    when (item) {
                        is MapItem.MapColoredItem.PathMapItem -> {
                            val bakedPaths = pathBaker?.bakePath(item.paint, item.path.vertices)

                            PreparedItem.PreparedColoredItem.PreparedPathItem(
                                pointsToDoubleArray(item.path.vertices),
                                inner,
                                border,
                                item.minZoom,
                                innerPath = bakedPaths?.first,
                                outerPath = bakedPaths?.second,
                            )
                        }
                        is MapItem.MapColoredItem.PolygonMapItem -> {
                            val pointsArray = pointsToDoubleArray(item.polygon.vertices)
                            PreparedItem.PreparedColoredItem.PreparedPolygonItem(
                                pointsArray,
                                getTriangulation(pointsArray),
                                inner,
                                border,
                                item.minZoom,
                                FloatArray(item.polygon.vertices.size * 2)
                            )
                        }
                        is MapItem.MapColoredItem.CircleMapItem -> {
                            PreparedItem.PreparedColoredItem.PreparedCircleItem(
                                item.point,
                                item.radius,
                                inner,
                                border,
                                item.minZoom,
                            )
                        }
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

    private fun getTriangulation(pointsArray: DoubleArray) =
        if (isTriangulated) {
            Earcut.earcut(pointsArray, null, 2).toShortArray()
        } else {
            null
        }
}