package com.jacekpietras.mapview.logic

import android.content.Context
import android.graphics.Matrix
import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.RectD
import com.jacekpietras.mapview.logic.ItemVisibility.MOVED
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.model.ViewCoordinates
import com.jacekpietras.mapview.ui.LastMapUpdate.cutoE
import com.jacekpietras.mapview.ui.LastMapUpdate.cutoS
import com.jacekpietras.mapview.ui.LastMapUpdate.mergE
import com.jacekpietras.mapview.ui.LastMapUpdate.moveE
import com.jacekpietras.mapview.ui.MapRenderConfig
import com.jacekpietras.mapview.ui.MapRenderConfig.isDrawing
import com.jacekpietras.mapview.ui.PaintBaker
import com.jacekpietras.mapview.ui.compose.MapRenderer
import com.jacekpietras.mapview.ui.opengl.LinePolygonF
import com.jacekpietras.mapview.utils.doAnimation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.system.measureTimeMillis

class MapViewLogic<T>(
    context: Context,
    mapRenderer: MapRenderer,
    private val doAnimation: ((progress: Float) -> Unit) -> Unit = ::doAnimation,
    var invalidate: ((List<RenderItem<T>>) -> Unit)? = null,
    private var onStopCentering: (() -> Unit)? = null,
    private var onStartCentering: (() -> Unit)? = null,
    var setOnPointPlacedListener: ((PointD) -> Unit)? = null,
    val coroutineScope: CoroutineScope,
    antialiasing: Boolean = true,
) {

    init {
        MapRenderConfig.antialiasing = antialiasing
    }

    private val paintBaker = PaintBaker.Factory().create<T>(context, mapRenderer, antialiasing)

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
    private var worldPreparedListOfVisible: List<PreparedItem<T>> = emptyList()
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
    private var volatilePreparedListOfVisible: List<PreparedItem<T>> = emptyList()
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

    @Suppress("unused")
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

        if (!isDrawing.get()) {
            cutOutNotVisible()
        }
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
            visibleGpsCoordinate = ViewCoordinates(centerGpsCoordinate, zoom, currentWidth, currentHeight, worldRotation)
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

    fun redraw() {
        prevVisibleGpsCoordinate = null
        prevVisibleGpsCoordinateForBigDiff = null
        worldPreparedListMaker.clear()
        worldPreparedList = emptyList()
        worldPreparedListOfVisible = emptyList()
        volatilePreparedListMaker.clear()
        volatilePreparedList = emptyList()
        volatilePreparedListOfVisible = emptyList()
        invalidate?.invoke(emptyList())
    }

    private fun cutOutNotVisible() {
        cutoS = System.nanoTime()

        if (currentWidth == 0 || currentHeight == 0) return
        if (worldBounds.notInitialized()) return
        if (worldPreparedList.isEmpty()) return
        if (volatilePreparedList.isEmpty()) return

        if (cuttingOutNow.get()) return
        cuttingOutNow.set(true)

        establishViewCoordinates()
        checkMovementOfScene()

        moveE = System.nanoTime()

        makeNewRenderList()

        cutoE = System.nanoTime()

        cuttingOutNow.set(false)
    }

    private fun makeNewRenderList() {
        RenderListMaker<T>(
            visibleGpsCoordinate = visibleGpsCoordinate,
            currentWidth = currentWidth,
            zoom = zoom,
            centerGpsCoordinate = centerGpsCoordinate,
            bakeDimension = paintBaker::bakeDimension,
        )
            .translate(worldPreparedListOfVisible, volatilePreparedListOfVisible)
            .also {
                mergE = System.nanoTime()
                invalidate?.invoke(it)
            }
    }

    private fun checkMovementOfScene() {
        val worldMoved = visibleGpsCoordinate != prevVisibleGpsCoordinate
        val worldMovedALot = worldMoved && (prevVisibleGpsCoordinateForBigDiff?.printDiff(visibleGpsCoordinate) ?: true)
        if (worldMovedALot) {
            Timber.d("Perf: moved a lot")
            coroutineScope.launch(Dispatchers.Default) {
                worldPreparedListOfVisible = worldPreparedList.checkVisibilityOfAllItems()
                volatilePreparedListOfVisible = volatilePreparedList.checkVisibilityOfAllItems()

                if (!cuttingOutNow.get()) {
                    withContext(Dispatchers.Main) {
                        makeNewRenderList()
                    }
                }
            }
            prevVisibleGpsCoordinateForBigDiff = visibleGpsCoordinate
        } else if (worldMoved) {
            clearTranslatedCache()
        }
        prevVisibleGpsCoordinate = visibleGpsCoordinate
    }

    private fun clearTranslatedCache() {
        listOf(worldPreparedListOfVisible, volatilePreparedListOfVisible)
            .forEach { preparedItems ->
                preparedItems.forEach { item ->
                    item.visibility = MOVED
                }
            }
    }

    private fun <T> List<PreparedItem<T>>.checkVisibilityOfAllItems() =
        mapNotNull { item ->
            when (item) {
                is PreparedItem.PreparedColoredItem.PreparedPolygonItem -> {
                    if (item.minZoom.isBiggerThanZoom() && visibleGpsCoordinate.isPolygonVisible(item.shape)) {
                        item.visibility = MOVED
                        item
                    } else {
                        null
                    }
                }
                is PreparedItem.PreparedColoredItem.PreparedPathItem -> {
                    val visiblePaths = visibleGpsCoordinate.getVisiblePath(item.shape, item.linePolygon)
                    if (item.minZoom.isBiggerThanZoom() && visiblePaths != null) {
                        val (visiblePath, visibleLinePolygons) = visiblePaths
                        item.visibility = MOVED
                        item.visibleParts = visiblePath
                        item.visibleLinePolygons = visibleLinePolygons
                        item.cacheTranslated = visiblePath.map { FloatArray(it.size) }
                        item.cacheLinePolygonsTranslated = visibleLinePolygons?.map { LinePolygonF.create(size = it.strip.array.size) }
                        item
                    } else {
                        null
                    }
                }
                is PreparedItem.PreparedColoredItem.PreparedCircleItem -> {
                    if (item.minZoom.isBiggerThanZoom() && visibleGpsCoordinate.isPointVisible(item.point)) {
                        item.visibility = MOVED
                        item
                    } else {
                        null
                    }
                }
                is PreparedItem.PreparedBitmapItem -> {
                    if (item.minZoom.isBiggerThanZoom() && visibleGpsCoordinate.isPointVisible(item.point)) {
                        item.visibility = MOVED
                        item
                    } else {
                        null
                    }
                }
            }
        }

    private fun Float?.isBiggerThanZoom(): Boolean =
        this == null || this > zoom
}