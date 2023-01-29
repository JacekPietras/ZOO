package com.jacekpietras.mapview.logic

import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PaintHolder
import com.jacekpietras.mapview.ui.MapRenderConfig.isTriangulated
import com.jacekpietras.mapview.ui.PaintBaker
import com.jacekpietras.mapview.utils.pointsToDoubleArray
import com.jacekpietras.mapview.utils.toShortArray
import earcut4j.Earcut


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
                        is MapItem.MapColoredItem.PathMapItem -> {
                            PreparedItem.PreparedColoredItem.PreparedPathItem(
                                pointsToDoubleArray(item.path.vertices),
                                inner,
                                border,
                                item.minZoom,
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