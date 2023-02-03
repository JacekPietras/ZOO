package com.jacekpietras.mapview.logic

import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.logic.ItemVisibility.CACHED
import com.jacekpietras.mapview.logic.PreparedItem.PreparedBitmapItem
import com.jacekpietras.mapview.logic.PreparedItem.PreparedColoredItem.PreparedCircleItem
import com.jacekpietras.mapview.logic.PreparedItem.PreparedColoredItem.PreparedPathItem
import com.jacekpietras.mapview.logic.PreparedItem.PreparedColoredItem.PreparedPolygonItem
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.PaintHolder
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.model.ViewCoordinates
import com.jacekpietras.mapview.ui.LastMapUpdate.cachE
import com.jacekpietras.mapview.ui.LastMapUpdate.sortE
import com.jacekpietras.mapview.ui.LastMapUpdate.sortS
import com.jacekpietras.mapview.ui.LastMapUpdate.tranS

internal class RenderListMaker<T>(
    private val visibleGpsCoordinate: ViewCoordinates,
    private val currentWidth: Int,
    private val zoom: Double,
    private val centerGpsCoordinate: PointD,
    private val bakeDimension: (MapDimension) -> (Double, PointD, Int) -> Float,
) {

    private val borders = mutableListOf<RenderItem<T>>()
    private val insides = mutableListOf<RenderItem<T>>()
    private val icons = mutableListOf<RenderItem.PointItem<T>>()
    private val dynamicPaints = mutableMapOf<PaintHolder.Dynamic<T>, T>()
    private val dynamicDimensions = mutableMapOf<MapDimension, Float>()

    fun translate(vararg preparedLists: List<PreparedItem<T>>): List<RenderItem<T>> {
        tranS = System.nanoTime()
        preparedLists.forEach(::makeCache)
        cachE = System.nanoTime()
        preparedLists.forEach(::addToRenderItems)
        sortS = System.nanoTime()
        icons.sortBy { it.cY }
        sortE = System.nanoTime()
        return borders + insides + icons
    }

    private fun addToRenderItems(preparedList: List<PreparedItem<T>>) {
        preparedList.forEach { item ->
            if (item.visibility == CACHED) {
                when (item) {
                    is PreparedPolygonItem.Plain -> {
                        item.addToRender(item.cacheTranslated)
                    }
                    is PreparedPolygonItem.Block -> {
                        item.addToRender(item.cacheRoofTranslated)
                    }
                    is PreparedPathItem -> {
                        item.cacheTranslated!!.forEach { item.addToRender(it) }
                    }
                    is PreparedCircleItem -> {
                        item.addToRender(item.cacheTranslated)
                    }
                    is PreparedBitmapItem -> {
                        item.addToRender(item.cacheTranslated)
                    }
                }
            }
        }
    }

    private fun makeCache(preparedList: List<PreparedItem<T>>) {
        preparedList.forEach { item ->
            when (item) {
                is PreparedPolygonItem -> {
                    if (item.visibility != CACHED) {
                        visibleGpsCoordinate.transformPolygon(item.shape, item.cacheTranslated)
                        if (item is PreparedPolygonItem.Block) {
                            visibleGpsCoordinate.transformRoofPolygon(item.cacheTranslated, item.cacheRoofTranslated)
                        }
                        item.visibility = CACHED
                    }
                }
                is PreparedPathItem -> {
                    if (item.visibility != CACHED) {
                        visibleGpsCoordinate.transformPath(item.cacheRaw!!, item.cacheTranslated!!)
                        item.visibility = CACHED
                    }
                }
                is PreparedCircleItem -> {
                    if (item.visibility != CACHED) {
                        visibleGpsCoordinate.transformPoint(item.point, item.cacheTranslated)
                        item.visibility = CACHED
                    }
                }
                is PreparedBitmapItem -> {
                    if (item.visibility != CACHED) {
                        visibleGpsCoordinate.transformPoint(item.point, item.cacheTranslated)
                        item.visibility = CACHED
                    }
                }
            }
        }
    }

    private fun PreparedPolygonItem<T>.addToRender(
        array: FloatArray,
    ) {
        insides.add(
            RenderItem.RenderPolygonItem(
                array,
                paintHolder.takePaint(),
            )
        )
        outerPaintHolder?.let { outerPaintHolder ->
            borders.add(
                RenderItem.RenderPolygonItem(
                    array,
                    outerPaintHolder.takePaint(),
                )
            )
        }
    }

    private fun PreparedPathItem<T>.addToRender(
        array: FloatArray,
    ) {
        insides.add(
            RenderItem.RenderPathItem(
                array,
                paintHolder.takePaint(),
            )
        )
        if (outerPaintHolder != null) {
            borders.add(
                RenderItem.RenderPathItem(
                    array,
                    outerPaintHolder.takePaint(),
                )
            )
        }
    }

    private fun PreparedCircleItem<T>.addToRender(
        array: FloatArray,
    ) {
        insides.add(
            RenderItem.PointItem.RenderCircleItem(
                array[0],
                array[1],
                radius.takeDimension(),
                paintHolder.takePaint(),
            )
        )
        if (outerPaintHolder != null) {
            borders.add(
                RenderItem.PointItem.RenderCircleItem(
                    array[0],
                    array[1],
                    radius.takeDimension(),
                    outerPaintHolder.takePaint(),
                )
            )
        }
    }

    private fun PreparedBitmapItem<T>.addToRender(
        array: FloatArray,
    ) {
        icons.add(
            RenderItem.PointItem.RenderBitmapItem(
                array[0],
                array[1],
                bitmap,
                pivot,
            )
        )
    }

    private fun MapDimension.takeDimension(): Float =
        dynamicDimensions[this]
            ?: bakeDimension(this)(zoom, centerGpsCoordinate, currentWidth)
                .also { dynamicDimensions[this] = it }

    private fun PaintHolder<T>.takePaint(): T =
        when (this) {
            is PaintHolder.Static<T> -> {
                paint
            }
            is PaintHolder.Dynamic<T> -> {
                dynamicPaints[this]
                    ?: block(zoom, centerGpsCoordinate, currentWidth)
                        .also { dynamicPaints[this] = it }
            }
        }
}