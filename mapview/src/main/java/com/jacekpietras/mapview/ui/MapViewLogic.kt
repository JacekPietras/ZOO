package com.jacekpietras.mapview.ui

import android.graphics.Matrix
import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.model.*
import com.jacekpietras.mapview.utils.pointsToDoubleArray
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class MapViewLogic<T>(
    private val doAnimation: ((progress: Float, left: Float) -> Unit) -> Unit,
    private val invalidate: () -> Unit,
    private val bakeCanvasPaint: (MapPaint) -> PaintHolder<T>,
    private val bakeBorderCanvasPaint: (MapPaint) -> PaintHolder<T>?,
    var currentHeight: Int = 0,
    var currentWidth: Int = 0,
    var setOnPointPlacedListener: ((PointD) -> Unit)? = null,
) {

    private var maxZoom: Double = 10.0
    private var minZoom: Double = 2.0
    var worldBounds: RectD = RectD()
        set(value) {
            field = value
            centerGpsCoordinate = PointD(value.centerX(), value.centerY())
            maxZoom = min(abs(value.width()), abs(value.height())) / 2
            minZoom = maxZoom / 10
            zoom = maxZoom / 5
        }
    private var _objectList: List<ObjectItem<T>> = emptyList()
    var objectList: List<MapItem> = emptyList()
        set(value) {
            field = value
            _objectList = value.toRenderItems()
            cutOutNotVisible()
        }

    var userPosition: PointD? = null
        set(value) {
            field = value
            if (centeringAtUser) {
                centerAtUserPosition()
            }
        }
    var terminalPoints: List<PointD> = emptyList()
        set(value) {
            field = value
            cutOutNotVisible()
        }
    private var terminalPointsOnScreen: FloatArray? = null
    private var userPositionOnScreen: FloatArray? = null
    var compass: Float = 0f
        set(value) {
            field = value
            if (centeringAtUser) {
                centerAtUserCompass()
            }
        }

    private val interesting: List<PointD> = listOf()
    private var interestingOnScreen: FloatArray? = null

    var clickOnWorld: PointD? = null
        set(value) {
            field = value
            cutOutNotVisible()
        }
    var shortestPath: List<PointD> = emptyList()
        set(value) {
            field = value
            cutOutNotVisible()
        }
    private var shortestPathOnScreen: FloatArray? = null
    private var clickOnScreen: FloatArray? = null

    private lateinit var visibleGpsCoordinate: ViewCoordinates
    private var centerGpsCoordinate: PointD =
        PointD(worldBounds.centerX(), worldBounds.centerY())
    private var zoom: Double = 5.0
    private var zoomOnStart: Double = 5.0
    private var worldRotation: Float = 0f
    private var worldRotationOnStart: Float = 0f
    internal var renderList: List<RenderItem<T>>? = null
    private var centeringAtUser = false

    fun centerAtPoint(desiredPosition: PointD) {
        centeringAtUser = false
        val previousPosition = centerGpsCoordinate

        doAnimation { progress, left ->
            centerGpsCoordinate = previousPosition * left + desiredPosition * progress
            cutOutNotVisible()
        }
    }

    fun centerAtUserPosition() {
        centeringAtUser = true
        val desiredPosition = userPosition ?: return
        val previousPosition = centerGpsCoordinate

        doAnimation { progress, left ->
            centerGpsCoordinate = previousPosition * left + desiredPosition * progress
            cutOutNotVisible()
        }
    }

    private fun centerAtUserCompass() {
        val diff = worldRotation - compass
        val previousPosition = when {
            diff > 180 -> worldRotation - 360
            diff < -180 -> worldRotation + 360
            else -> worldRotation
        }

        doAnimation { progress, left ->
            worldRotation = (previousPosition * left + compass * progress) % 360f
            cutOutNotVisible()
        }
    }

    fun onSizeChanged() {
        cutOutNotVisible(invalidate = false)
    }

    fun onScaleBegin() {
        zoomOnStart = zoom
    }

    fun onScale(scale: Float) {
        val zoomPoint = (maxZoom - minZoom) / 2
        zoom = (zoomOnStart + zoomPoint * (1 - scale)).coerceAtMost(maxZoom).coerceAtLeast(minZoom)
        cutOutNotVisible()
    }

    fun onRotateBegin() {
        worldRotationOnStart = worldRotation
    }

    fun onRotate(rotate: Float) {
        worldRotation = (rotate + worldRotationOnStart) % 360
    }

    fun onScroll(vX: Float, vY: Float) {
        centeringAtUser = false
        val radians = Math.toRadians(-worldRotation.toDouble())
        centerGpsCoordinate += PointD(
            (sin(radians) * vY + cos(radians) * vX) / visibleGpsCoordinate.horizontalScale,
            (-sin(radians) * vX + cos(radians) * vY) / visibleGpsCoordinate.verticalScale
        )
        cutOutNotVisible()
    }

    fun onClick(x: Float, y: Float) {
        val point = FloatArray(2)
        point[0] = x
        point[1] = y
        Matrix().apply {
            setRotate(
                worldRotation,
                currentWidth / 2.toFloat(),
                currentHeight / 2.toFloat(),
            )
            mapPoints(point)
        }

        setOnPointPlacedListener?.invoke(visibleGpsCoordinate.deTransformPoint(point[0], point[1]))
    }

    fun draw(
        drawPath: (shape: FloatArray, paint: T, close: Boolean) -> Unit,
        drawCircle: (cx: Float, xy: Float, radius: Float, paint: T) -> Unit,
        userPositionPaint: T,
        terminalPaint: T,
        shortestPaint: T,
        interestingPaint: T,
    ) {

        renderList?.forEach { drawPath(it.shape, it.paint, it.close) }
        userPositionOnScreen?.let {
            drawCircle(it[0], it[1], 15f, userPositionPaint)
        }
        terminalPointsOnScreen?.let { array ->
            for (i in array.indices step 2) {
                drawCircle(array[i], array[i + 1], 5f, terminalPaint)
            }
        }
        shortestPathOnScreen?.let { array ->
            drawPath(array, shortestPaint, false)
        }
        interestingOnScreen?.let { array ->
            for (i in array.indices step 2) {
                drawCircle(array[i], array[i + 1], 5f, interestingPaint)
            }
        }
        clickOnScreen?.let {
            drawCircle(it[0], it[1], 15f, interestingPaint)
        }
    }

    private fun List<MapItem>.toRenderItems(): List<ObjectItem<T>> {
        val innerPaints = mutableMapOf<MapPaint, PaintHolder<T>>()
        val borderPaints = mutableMapOf<MapPaint, PaintHolder<T>?>()

        return map { item ->
            val inner = innerPaints[item.paint]
                ?: bakeCanvasPaint(item.paint)
                    .also { innerPaints[item.paint] = it }
            val border = borderPaints[item.paint]
                ?: bakeBorderCanvasPaint(item.paint)
                    .also { borderPaints[item.paint] = it }

            when (item.shape) {
                is PathD -> ObjectItem(
                    pointsToDoubleArray(item.shape.vertices),
                    inner,
                    border,
                    close = false,
                )
                is PolygonD -> ObjectItem(
                    pointsToDoubleArray(item.shape.vertices),
                    inner,
                    border,
                    close = true,
                )
                else -> throw IllegalStateException("Unknown shape type ${item.shape}")
            }
        }
    }

    private fun preventedGoingOutsideWorld(): Boolean {
        val reversedV = worldBounds.top > worldBounds.bottom
        val reversedH = worldBounds.left > worldBounds.right

        val leftMargin =
            if (reversedH) {
                worldBounds.left - visibleGpsCoordinate.visibleRect.left
            } else {
                visibleGpsCoordinate.visibleRect.left - worldBounds.left
            }
        val rightMargin =
            if (reversedH) {
                visibleGpsCoordinate.visibleRect.right - worldBounds.right
            } else {
                worldBounds.right - visibleGpsCoordinate.visibleRect.right
            }
        val topMargin =
            if (reversedV) {
                worldBounds.top - visibleGpsCoordinate.visibleRect.top
            } else {
                visibleGpsCoordinate.visibleRect.top - worldBounds.top
            }
        val bottomMargin =
            if (reversedV) {
                visibleGpsCoordinate.visibleRect.bottom - worldBounds.bottom
            } else {
                worldBounds.bottom - visibleGpsCoordinate.visibleRect.bottom
            }
        var result = false

        when {
            leftMargin + rightMargin < 0 -> {
                zoom = zoom.times(.99f).coerceAtMost(maxZoom).coerceAtLeast(minZoom)
                centerGpsCoordinate = PointD(worldBounds.centerX(), centerGpsCoordinate.y)
                result = true
            }
            leftMargin < -0.00001 -> {
                if (reversedH) {
                    centerGpsCoordinate += PointD(leftMargin, 0.0)
                } else {
                    centerGpsCoordinate -= PointD(leftMargin, 0.0)
                }
                result = true
            }
            rightMargin < -0.00001 -> {
                if (reversedH) {
                    centerGpsCoordinate -= PointD(rightMargin, 0.0)
                } else {
                    centerGpsCoordinate += PointD(rightMargin, 0.0)
                }
                result = true
            }
        }

        when {
            topMargin + bottomMargin < 0 -> {
                zoom = zoom.times(.99f).coerceAtMost(maxZoom).coerceAtLeast(minZoom)
                centerGpsCoordinate = PointD(centerGpsCoordinate.x, worldBounds.centerY())
                result = true
            }
            topMargin < -0.00001 -> {
                if (reversedV) {
                    centerGpsCoordinate += PointD(0.0, topMargin)
                } else {
                    centerGpsCoordinate -= PointD(0.0, topMargin)
                }
                result = true
            }
            bottomMargin < -0.00001 -> {
                if (reversedV) {
                    centerGpsCoordinate -= PointD(0.0, bottomMargin)
                } else {
                    centerGpsCoordinate += PointD(0.0, bottomMargin)
                }
                result = true
            }
        }

        return result
    }

    private fun PaintHolder<T>.takePaint(dynamicPaints: MutableMap<PaintHolder.Dynamic<T>, T>): T {
        return when (this) {
            is PaintHolder.Static<T> -> paint
            is PaintHolder.Dynamic<T> -> {
                dynamicPaints[this]
                    ?: block(zoom, centerGpsCoordinate, currentWidth)
                        .also { dynamicPaints[this] = it }
            }
        }
    }

    private fun cutOutNotVisible(invalidate: Boolean = true) {
        if (currentWidth == 0 || currentHeight == 0) return
        if (worldBounds.width() == 0.0 || worldBounds.height() == 0.0) return

        visibleGpsCoordinate = ViewCoordinates(centerGpsCoordinate, zoom, currentWidth, currentHeight)

        if (preventedGoingOutsideWorld()) {
            cutOutNotVisible(false)
            return
        }

        val borders = mutableListOf<RenderItem<T>>()
        val insides = mutableListOf<RenderItem<T>>()
        val dynamicPaints = mutableMapOf<PaintHolder.Dynamic<T>, T>()

        val matrix = Matrix()
            .apply {
                setRotate(
                    -worldRotation,
                    currentWidth / 2.toFloat(),
                    currentHeight / 2.toFloat(),
                )
            }

        _objectList.forEach { item ->
            if (item.close) {
                visibleGpsCoordinate
                    .transformPolygon(item.shape)
                    ?.withMatrix(matrix, worldRotation)
                    ?.let { polygon ->
                        item.addToRender(polygon, borders, insides, dynamicPaints)
                    }
            } else {
                visibleGpsCoordinate
                    .transformPath(item.shape)
                    .map { it.withMatrix(matrix, worldRotation) }
                    .forEach { path ->
                        item.addToRender(path, borders, insides, dynamicPaints)
                    }
            }
        }
        userPosition?.let {
            userPositionOnScreen = visibleGpsCoordinate
                .transformPoint(it)
                .withMatrix(matrix, worldRotation)
        }
        clickOnWorld?.let {
            clickOnScreen = visibleGpsCoordinate
                .transformPoint(it)
                .withMatrix(matrix, worldRotation)
        }
        if (terminalPoints.isNotEmpty()) {
            terminalPointsOnScreen = visibleGpsCoordinate
                .transformPoints(terminalPoints)
                .withMatrix(matrix, worldRotation)
        }
        if (shortestPath.isNotEmpty()) {
            shortestPathOnScreen = visibleGpsCoordinate
                .transformPoints(shortestPath)
                .withMatrix(matrix, worldRotation)
        }
        if (interesting.isNotEmpty()) {
            interestingOnScreen = visibleGpsCoordinate
                .transformPoints(interesting)
                .withMatrix(matrix, worldRotation)
        }

        renderList = borders + insides
//        logVisibleShapes()
        if (invalidate) invalidate()
    }

    private fun FloatArray.withMatrix(matrix: Matrix, worldRotation: Float): FloatArray {
        if (worldRotation != 0f) {
            matrix.mapPoints(this)
        }
        return this
    }

    private fun ObjectItem<T>.addToRender(
        array: FloatArray,
        borders: MutableList<RenderItem<T>>,
        insides: MutableList<RenderItem<T>>,
        dynamicPaints: MutableMap<PaintHolder.Dynamic<T>, T>,
    ) {
        insides.add(
            RenderItem(
                array,
                paintHolder.takePaint(dynamicPaints),
                close
            )
        )
        if (outerPaintHolder != null) {
            borders.add(
                RenderItem(
                    array,
                    outerPaintHolder.takePaint(dynamicPaints),
                    close
                )
            )
        }
    }

//    private fun logVisibleShapes() {
//        if (BuildConfig.DEBUG) {
//            val message = "Preparing render list: " +
//                    renderList?.map { "Shape " + (it.shape.size shr 1) } +
//                    " (${objectList.size})"
//            Timber.v(message)
//        }
//    }

    private class ObjectItem<T>(
        val shape: DoubleArray,
        val paintHolder: PaintHolder<T>,
        val outerPaintHolder: PaintHolder<T>? = null,
        val close: Boolean,
    )

    internal class RenderItem<T>(
        val shape: FloatArray,
        val paint: T,
        val close: Boolean,
    )
}
