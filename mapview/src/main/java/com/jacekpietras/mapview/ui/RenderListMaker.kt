package com.jacekpietras.mapview.ui

import android.graphics.Matrix
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.PaintHolder
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

    private val borders = mutableListOf<MapViewLogic.RenderItem<T>>()
    private val insides = mutableListOf<MapViewLogic.RenderItem<T>>()
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

    fun translate(preparedList: List<MapViewLogic.PreparedItem<T>>): List<MapViewLogic.RenderItem<T>> {
        borders.clear()
        insides.clear()

        preparedList.forEach { item ->
            when (item) {
                is MapViewLogic.PreparedItem.PreparedPolygonItem -> {
                    visibleGpsCoordinate
                        .transformPolygon(item.shape)
                        ?.withMatrix(matrix, worldRotation)
                        ?.let { polygon ->
                            item.addToRender(polygon)
                        }
                }
                is MapViewLogic.PreparedItem.PreparedPathItem -> {
                    visibleGpsCoordinate
                        .transformPath(item.shape)
                        .map { it.withMatrix(matrix, worldRotation) }
                        .forEach { path ->
                            item.addToRender(path)
                        }
                }
                is MapViewLogic.PreparedItem.PreparedCircleItem -> {
                    visibleGpsCoordinate
                        .transformPoint(item.point)
                        .withMatrix(matrix, worldRotation)
                        .let { path ->
                            item.addToRender(path)
                        }
                }
            }
        }

        return borders + insides
    }

    private fun FloatArray.withMatrix(matrix: Matrix, worldRotation: Float): FloatArray {
        if (worldRotation != 0f) {
            matrix.mapPoints(this)
        }
        return this
    }

    private fun MapViewLogic.PreparedItem<T>.addToRender(
        array: FloatArray,
    ) {
        when (this) {
            is MapViewLogic.PreparedItem.PreparedPolygonItem -> {
                insides.add(
                    MapViewLogic.RenderItem.RenderPolygonItem(
                        array,
                        paintHolder.takePaint(),
                    )
                )
                if (outerPaintHolder != null) {
                    borders.add(
                        MapViewLogic.RenderItem.RenderPolygonItem(
                            array,
                            outerPaintHolder!!.takePaint(),
                        )
                    )
                }
            }
            is MapViewLogic.PreparedItem.PreparedPathItem -> {
                insides.add(
                    MapViewLogic.RenderItem.RenderPathItem(
                        array,
                        paintHolder.takePaint(),
                    )
                )
                if (outerPaintHolder != null) {
                    borders.add(
                        MapViewLogic.RenderItem.RenderPathItem(
                            array,
                            outerPaintHolder!!.takePaint(),
                        )
                    )
                }
            }
            is MapViewLogic.PreparedItem.PreparedCircleItem -> {
                insides.add(
                    MapViewLogic.RenderItem.RenderCircleItem(
                        array[0],
                        array[1],
                        radius.takeDimension(),
                        paintHolder.takePaint(),
                    )
                )
                if (outerPaintHolder != null) {
                    borders.add(
                        MapViewLogic.RenderItem.RenderCircleItem(
                            array[0],
                            array[1],
                            radius.takeDimension(),
                            outerPaintHolder!!.takePaint(),
                        )
                    )
                }
            }
        }
    }

    private fun MapDimension.takeDimension(): Float =
        dynamicDimensions[this]
            ?: bakeDimension(this).invoke(zoom, centerGpsCoordinate, currentWidth)
                .also { dynamicDimensions[this] = it }

    private fun PaintHolder<T>.takePaint(): T {
        return when (this) {
            is PaintHolder.Static<T> -> paint
            is PaintHolder.Dynamic<T> -> {
                dynamicPaints[this]
                    ?: block(zoom, centerGpsCoordinate, currentWidth)
                        .also { dynamicPaints[this] = it }
            }
        }
    }
}