package com.jacekpietras.mapview.logic

import android.graphics.Matrix
import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.RectD
import com.jacekpietras.mapview.logic.PreparedItem.PreparedBitmapItem
import com.jacekpietras.mapview.logic.PreparedItem.PreparedColoredItem.PreparedCircleItem
import com.jacekpietras.mapview.logic.PreparedItem.PreparedColoredItem.PreparedPathItem
import com.jacekpietras.mapview.logic.PreparedItem.PreparedColoredItem.PreparedPolygonItem
import com.jacekpietras.mapview.logic.PreparedItem.PreparedIconItem
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.model.ViewCoordinates
import com.jacekpietras.mapview.ui.PaintBaker
import com.jacekpietras.mapview.utils.doAnimation
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.system.measureTimeMillis

class MapViewLogic<T>(
    private val doAnimation: ((progress: Float) -> Unit) -> Unit = ::doAnimation,
    private val invalidate: (List<RenderItem<T>>) -> Unit,
    var paintBaker: PaintBaker<T>,
    private var onStopCentering: (() -> Unit)? = null,
    private var onStartCentering: (() -> Unit)? = null,
    var setOnPointPlacedListener: ((PointD) -> Unit)? = null,
) {

    var worldData: WorldData = WorldData()
        set(value) {
            field = value

            if (!value.bounds.contains(centerGpsCoordinate)) {
                centerGpsCoordinate = PointD(value.bounds.centerX(), value.bounds.centerY())
            }
            maxZoom = min(abs(value.bounds.width()), abs(value.bounds.height())) / 1.6
            minZoom = maxZoom / 10

            val measured = measureTimeMillis {
                worldPreparedList = worldPreparedListMaker.toPreparedItems(value.objectList)
            }
            Timber.d("Perf: toPreparedItems $measured ms (world)")

            cutOutNotVisible()

            nextAnimation?.run { animateCentering(first, second) }
        }
    private val worldBounds: RectD get() = worldData.bounds
    private var worldPreparedList: List<PreparedItem<T>> = emptyList()
    private val worldPreparedListMaker = PreparedListMaker(paintBaker)

    var userData: UserData = UserData()
        set(value) {
            field = value

            val measured = measureTimeMillis {
                volatilePreparedList = volatilePreparedListMaker.toPreparedItems(value.objectList)
            }
            Timber.d("Perf: toPreparedItems $measured ms (user)")

            if (centeringAtUser) {
                centerAtUserPositionAndRotation()
            } else {
                cutOutNotVisible()
            }
        }

    private var volatilePreparedList: List<PreparedItem<T>> = emptyList()
    private val volatilePreparedListMaker = PreparedListMaker(paintBaker)
    private val userPosition: PointD? get() = userData.userPosition.takeIf { it != PointD() }
    private val compass: Float get() = userData.compass

    private var currentHeight: Int = 0
    private var currentWidth: Int = 0
    private var maxZoom: Double = 10.0
        set(value) {
            if (value != field) {
                field = value
                zoom = maxZoom / 3
            }
        }
    private var minZoom: Double = 2.0
        set(value) {
            if (value != field) {
                field = value
                zoom = maxZoom / 3
            }
        }
    private lateinit var visibleGpsCoordinate: ViewCoordinates
    private var prevVisibleGpsCoordinate: ViewCoordinates? = null
    private var prevVisibleGpsCoordinateForBigDiff: ViewCoordinates? = null
    private var centerGpsCoordinate: PointD = PointD()
    private var zoom: Double = 5.0
        set(value) {
            field = value.coerceAtMost(maxZoom).coerceAtLeast(minZoom)
        }
    private var worldRotation: Float = 0f
        set(value) {
            field = value % 360
        }
    private var centeringAtUser = false
        set(value) {
            if (field != value) {
                if (value) {
                    onStartCentering?.invoke()
                } else {
                    onStopCentering?.invoke()
                }
            }
            field = value
        }
    private val cuttingOutNow = AtomicBoolean(false)

    fun centerAtPoint(desiredPosition: PointD) {
        centeringAtUser = false
        animateCentering(desiredPosition)
    }

    fun centerAtUserPosition() {
        centeringAtUser = true
        val desiredPosition = userPosition ?: return
        animateCentering(desiredPosition)
    }

    private fun centerAtUserPositionAndRotation() {
        animateCentering(userPosition, compass)
    }

    private var nextAnimation: Pair<PointD?, Float?>? = null
    private val animatingNow = AtomicBoolean(false)
    private fun animateCentering(desiredPosition: PointD?, desiredRotation: Float? = null) {
        if (desiredPosition == null && desiredRotation == null) return
        if (animatingNow.get() || worldBounds.notInitialized()) {
            nextAnimation = Pair(desiredPosition, desiredRotation)
            return
        }
        animatingNow.set(true)
        nextAnimation = null
        val previousPosition = centerGpsCoordinate
        val previousRotation = getPreviousRotation()
        doAnimation { progress ->
            val left = 1f - progress
            if (desiredPosition != null) {
                centerGpsCoordinate = previousPosition * left + desiredPosition * progress
            }
            if (desiredRotation != null) {
                worldRotation = previousRotation * left + desiredRotation * progress
            }
            cutOutNotVisible()
            if (progress == 1f) {
                animatingNow.set(false)
                nextAnimation?.run { animateCentering(first, second) }
            }
        }
    }

    private fun getPreviousRotation(): Float {
        val diff = worldRotation - compass
        return when {
            diff > 180 -> worldRotation - 360
            diff < -180 -> worldRotation + 360
            else -> worldRotation
        }
    }

    fun onSizeChanged(width: Int, height: Int) {
        if (currentHeight != height || currentWidth != width) {
            currentHeight = height
            currentWidth = width
            cutOutNotVisible()
        }
    }

    fun onTransform(cX: Float, cY: Float, scale: Float, rotate: Float, vX: Float, vY: Float) {
        if (!::visibleGpsCoordinate.isInitialized) return

        val mX = pinchCorrection(cX, scale, currentWidth)
        val mY = pinchCorrection(cY, scale, currentHeight)

        zoom *= scale
        worldRotation += rotate
        centeringAtUser = false
        centerGpsCoordinate += toMovementInWorld(vX + mX, vY + mY)

        cutOutNotVisible()
    }

    private fun pinchCorrection(pinchPoint: Float, scale: Float, size: Int): Float {
        val toCenter = -pinchPoint / size + 0.5f
        val sizeChange = size * (scale - 1)
        return toCenter * sizeChange
    }

    fun onScale(cX: Float, cY: Float, scale: Float) {
        if (scale != 1f) {
            val mX = pinchCorrection(cX, scale, currentWidth)
            val mY = pinchCorrection(cY, scale, currentHeight)
            zoom *= scale
            if (::visibleGpsCoordinate.isInitialized && scale != Float.MAX_VALUE) {
                centerGpsCoordinate += toMovementInWorld(mX, mY)
            }
            cutOutNotVisible()
        }
    }

    fun setRotate(rotate: Float) {
        if (rotate != worldRotation) {
            worldRotation = rotate
            cutOutNotVisible()
        }
    }

    private fun toMovementInWorld(vX: Float, vY: Float): PointD =
        Math.toRadians(-worldRotation.toDouble())
            .let { radians ->
                PointD(
                    (sin(radians) * vY + cos(radians) * vX) / visibleGpsCoordinate.horizontalScale,
                    (-sin(radians) * vX + cos(radians) * vY) / visibleGpsCoordinate.verticalScale,
                )
            }

    fun onClick(x: Float, y: Float) {
        if (!::visibleGpsCoordinate.isInitialized) return

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

    private fun establishViewCoordinates() {
        do {
            visibleGpsCoordinate = ViewCoordinates(centerGpsCoordinate, zoom, currentWidth, currentHeight)
        } while (preventedGoingOutsideWorld())
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
                zoom *= 1 + leftMargin + rightMargin
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
                zoom *= 1 + topMargin + bottomMargin
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

    private fun cutOutNotVisible() {
        val before = System.currentTimeMillis()

        if (currentWidth == 0 || currentHeight == 0) return
        if (worldBounds.notInitialized()) return

        if (cuttingOutNow.get()) return
        cuttingOutNow.set(true)

        establishViewCoordinates()

        prevVisibleGpsCoordinate?.printDiff(visibleGpsCoordinate)

        val worldMoved = visibleGpsCoordinate != prevVisibleGpsCoordinate
        val worldMovedALot = worldMoved && (prevVisibleGpsCoordinateForBigDiff?.printDiff(visibleGpsCoordinate) ?: true)
        if (worldMovedALot) {
            Timber.d("Perf: moved a lot")
            clearHiddenPreparedItems()
            prevVisibleGpsCoordinateForBigDiff = visibleGpsCoordinate
        } else if (worldMoved) {
            clearCachedPreparedItems()
        }
        prevVisibleGpsCoordinate = visibleGpsCoordinate

        RenderListMaker<T>(
            visibleGpsCoordinate = visibleGpsCoordinate,
            worldRotation = worldRotation,
            currentWidth = currentWidth,
            currentHeight = currentHeight,
            zoom = zoom,
            centerGpsCoordinate = centerGpsCoordinate,
            bakeDimension = paintBaker::bakeDimension,
        )
            .translate(worldPreparedList, volatilePreparedList)
            .also { invalidate(it) }

        Timber.d("Perf: cutOutNotVisible ${System.currentTimeMillis() - before} ms")
        cuttingOutNow.set(false)
    }

    private fun clearCachedPreparedItems() {
        listOf(worldPreparedList, volatilePreparedList)
            .forEach { preparedItems ->
                preparedItems.forEach { item ->
                    when (item) {
                        is PreparedPolygonItem -> {
                            item.cache = null
                        }
                        is PreparedBitmapItem -> {
                            item.cache = null
                        }
                        is PreparedCircleItem -> {
                            item.cache = null
                        }
                        is PreparedPathItem -> {
                            item.cache = null
                        }
                        is PreparedIconItem -> {
                            item.cache = null
                        }
                    }
                }
            }
    }

    private fun clearHiddenPreparedItems() {
        listOf(worldPreparedList, volatilePreparedList)
            .forEach { preparedItems ->
                preparedItems.forEach { item ->
                    when (item) {
                        is PreparedPolygonItem -> {
                            item.cache = null
                        }
                        is PreparedBitmapItem -> {
                            item.cache = null
                        }
                        is PreparedCircleItem -> {
                            item.cache = null
                        }
                        is PreparedPathItem -> {
                            item.cache = null
                        }
                        is PreparedIconItem -> {
                            item.cache = null
                        }
                    }
                    item.isHidden = false
                }
            }
    }
}
