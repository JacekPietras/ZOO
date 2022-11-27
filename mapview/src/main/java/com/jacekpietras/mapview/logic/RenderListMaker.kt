package com.jacekpietras.mapview.logic

import android.graphics.Matrix
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.logic.PreparedItem.PreparedBitmapItem
import com.jacekpietras.mapview.logic.PreparedItem.PreparedColoredItem.PreparedCircleItem
import com.jacekpietras.mapview.logic.PreparedItem.PreparedColoredItem.PreparedPathItem
import com.jacekpietras.mapview.logic.PreparedItem.PreparedColoredItem.PreparedPolygonItem
import com.jacekpietras.mapview.logic.PreparedItem.PreparedIconItem
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.PaintHolder
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.model.ViewCoordinates

internal class RenderListMaker<T>(
    private val visibleGpsCoordinate: ViewCoordinates,
    private val worldRotation: Float,
    private val currentWidth: Int,
    private val currentHeight: Int,
    private val zoom: Double,
    private val centerGpsCoordinate: PointD,
    private val bakeDimension: (MapDimension) -> ((Double, PointD, Int) -> Float),
) {

    private val borders = mutableListOf<RenderItem<T>>()
    private val insides = mutableListOf<RenderItem<T>>()
    private val icons = mutableListOf<RenderItem.PointItem<T>>()
    private val dynamicPaints = mutableMapOf<PaintHolder.Dynamic<T>, T>()
    private val dynamicDimensions = mutableMapOf<MapDimension, Float>()
    private val matrix = Matrix()
        .apply {
            setRotate(
                -worldRotation,
                currentWidth / 2.toFloat(),
                currentHeight / 2.toFloat(),
            )
        }

    fun translate(vararg preparedLists: List<PreparedItem<T>>): List<RenderItem<T>> {
        preparedLists.forEach(::addToRenderItems)
        icons.sortBy { it.cY }
        return borders + insides + icons
    }

    private fun addToRenderItems(preparedList: List<PreparedItem<T>>) {
        preparedList.forEach { item ->
            if (!item.minZoom.isBiggerThanZoom()) {
                return@forEach
            }

            when (item) {
                is PreparedPolygonItem -> {
                    visibleGpsCoordinate
                        .transformPolygon(item.shape)
                        ?.withMatrix(matrix, worldRotation)
                        ?.let { polygon ->
                            item.addToRender(polygon)
                        }
                }
                is PreparedPathItem -> {
                    visibleGpsCoordinate
                        .transformPath(item.shape)
                        .forEach { path ->
                            val polygon = path.withMatrix(matrix, worldRotation)
                            item.addToRender(polygon)
                        }
                }
                is PreparedCircleItem -> {
                    visibleGpsCoordinate
                        .transformPoint(item.point)
                        ?.withMatrix(matrix, worldRotation)
                        ?.let { point ->
                            item.addToRender(point)
                        }
                }
                is PreparedIconItem -> {
                    visibleGpsCoordinate
                        .transformPoint(item.point)
                        ?.withMatrix(matrix, worldRotation)
                        ?.let { point ->
                            item.addToRender(point)
                        }
                }
                is PreparedBitmapItem -> {
                    visibleGpsCoordinate
                        .transformPoint(item.point)
                        ?.withMatrix(matrix, worldRotation)
                        ?.let { point ->
                            item.addToRender(point)
                        }
                }
            }
        }
    }

    private fun Float?.isBiggerThanZoom(): Boolean =
        this == null || this > zoom

    private fun FloatArray.withMatrix(matrix: Matrix, worldRotation: Float): FloatArray {
        if (worldRotation != 0f) {
            matrix.mapPoints(this)
        }
        return this
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
        if (outerPaintHolder != null) {
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

    private fun PreparedIconItem<T>.addToRender(
        array: FloatArray,
    ) {
        icons.add(
            RenderItem.PointItem.RenderIconItem(
                array[0],
                array[1],
                icon,
            )
        )
    }

    private fun PreparedBitmapItem<T>.addToRender(
        array: FloatArray,
    ) {
        icons.add(
            RenderItem.PointItem.RenderBitmapItem(
                array[0],
                array[1],
                bitmap,
            )
        )
    }

    private fun MapDimension.takeDimension(): Float =
        dynamicDimensions[this]
            ?: bakeDimension(this).invoke(zoom, centerGpsCoordinate, currentWidth)
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