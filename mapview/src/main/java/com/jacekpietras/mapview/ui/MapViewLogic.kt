package com.jacekpietras.mapview.ui

import android.graphics.Matrix
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.RectD
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PaintHolder
import com.jacekpietras.mapview.model.ViewCoordinates
import com.jacekpietras.mapview.ui.MapViewLogic.PreparedItem.PreparedColoredItem.PreparedCircleItem
import com.jacekpietras.mapview.ui.MapViewLogic.PreparedItem.PreparedColoredItem.PreparedPathItem
import com.jacekpietras.mapview.ui.MapViewLogic.PreparedItem.PreparedColoredItem.PreparedPolygonItem
import com.jacekpietras.mapview.utils.doAnimation
import com.jacekpietras.mapview.utils.pointsToDoubleArray
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class MapViewLogic<T>(
    private val doAnimation: ((progress: Float) -> Unit) -> Unit = ::doAnimation,
    private val invalidate: (List<RenderItem<T>>) -> Unit,
    private val bakeCanvasPaint: (MapPaint) -> PaintHolder<T>,
    private val bakeBorderCanvasPaint: (MapPaint) -> PaintHolder<T>?,
    private val bakeDimension: (MapDimension) -> ((Double, PointD, Int) -> Float),
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

            worldPreparedList = value.objectList.toPreparedItems()

            cutOutNotVisible()

            nextAnimation?.run { animateCentering(first, second) }
        }
    private val worldBounds: RectD get() = worldData.bounds
    private var worldPreparedList: List<PreparedItem<T>> = emptyList()

    var userData: UserData = UserData()
        set(value) {
            field = value

            volatilePreparedList = value.objectList.toPreparedItems()

            if (centeringAtUser) {
                centerAtUserPositionAndRotation()
            } else {
                cutOutNotVisible()
            }
        }

    private var volatilePreparedList: List<PreparedItem<T>> = emptyList()
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
    private var centerGpsCoordinate: PointD = PointD()
    private var zoom: Double = 5.0
        set(value) {
            field = value.coerceAtMost(maxZoom).coerceAtLeast(minZoom)
        }
    private var worldRotation: Float = 0f
        set(value) {
            field = value % 360
        }
    var renderList: List<RenderItem<T>>? = null
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

    fun onRotate(rotate: Float) {
        if (rotate != 0f) {
            worldRotation += rotate
            cutOutNotVisible()
        }
    }

    fun setRotate(rotate: Float) {
        if (rotate != worldRotation) {
            worldRotation = rotate
            cutOutNotVisible()
        }
    }

    fun onScroll(vX: Float, vY: Float) {
        if (vX != 0f || vY != 0f) {
            centeringAtUser = false
            centerGpsCoordinate += toMovementInWorld(vX, vY)
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

    private fun List<MapItem>.toPreparedItems(): List<PreparedItem<T>> {
        val innerPaints = mutableMapOf<MapPaint, PaintHolder<T>>()
        val borderPaints = mutableMapOf<MapPaint, PaintHolder<T>?>()

        return map { item ->
            when (item) {
                is MapItem.MapColoredItem -> {
                    val inner = innerPaints[item.paint]
                        ?: bakeCanvasPaint(item.paint)
                            .also { innerPaints[item.paint] = it }
                    val border = borderPaints[item.paint]
                        ?: bakeBorderCanvasPaint(item.paint)
                            .also { borderPaints[item.paint] = it }

                    when (item) {
                        is MapItem.MapColoredItem.PathMapItem -> PreparedPathItem(
                            pointsToDoubleArray(item.path.vertices),
                            inner,
                            border,
                            item.minZoom,
                        )
                        is MapItem.MapColoredItem.PolygonMapItem -> PreparedPolygonItem(
                            pointsToDoubleArray(item.polygon.vertices),
                            inner,
                            border,
                            item.minZoom,
                        )
                        is MapItem.MapColoredItem.CircleMapItem -> PreparedCircleItem(
                            item.point,
                            item.radius,
                            inner,
                            border,
                            item.minZoom,
                        )
                    }
                }
                is MapItem.IconMapItem -> PreparedItem.PreparedIconItem(
                    item.point,
                    item.icon,
                    item.minZoom,
                )
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

    private fun cutOutNotVisible(invalidate: Boolean = true) {
        if (currentWidth == 0 || currentHeight == 0) return
        if (worldBounds.notInitialized()) return

        visibleGpsCoordinate = ViewCoordinates(centerGpsCoordinate, zoom, currentWidth, currentHeight)

        while (preventedGoingOutsideWorld()) {
            cutOutNotVisible(false)
        }
        if (!invalidate) {
            return
        }

        renderList = RenderListMaker<T>(
            visibleGpsCoordinate = visibleGpsCoordinate,
            worldRotation = worldRotation,
            currentWidth = currentWidth,
            currentHeight = currentHeight,
            zoom = zoom,
            centerGpsCoordinate = centerGpsCoordinate,
            bakeDimension = bakeDimension,
        ).translate(worldPreparedList + volatilePreparedList)

        invalidate(renderList!!)
    }

    internal sealed class PreparedItem<T>(
        open val minZoom: Float?,
    ) {

        internal sealed class PreparedColoredItem<T>(
            open val paintHolder: PaintHolder<T>,
            open val outerPaintHolder: PaintHolder<T>?,
            override val minZoom: Float?,
        ) : PreparedItem<T>(minZoom) {

            class PreparedPathItem<T>(
                val shape: DoubleArray,
                override val paintHolder: PaintHolder<T>,
                override val outerPaintHolder: PaintHolder<T>? = null,
                override val minZoom: Float? = null,
            ) : PreparedColoredItem<T>(paintHolder, outerPaintHolder, minZoom)

            class PreparedPolygonItem<T>(
                val shape: DoubleArray,
                override val paintHolder: PaintHolder<T>,
                override val outerPaintHolder: PaintHolder<T>? = null,
                override val minZoom: Float? = null,
            ) : PreparedColoredItem<T>(paintHolder, outerPaintHolder, minZoom)

            class PreparedCircleItem<T>(
                val point: PointD,
                val radius: MapDimension,
                override val paintHolder: PaintHolder<T>,
                override val outerPaintHolder: PaintHolder<T>? = null,
                override val minZoom: Float? = null,
            ) : PreparedColoredItem<T>(paintHolder, outerPaintHolder, minZoom)
        }

        class PreparedIconItem<T>(
            val point: PointD,
            @DrawableRes val icon: Int,
            override val minZoom: Float? = null,
        ) : PreparedItem<T>(minZoom)
    }

    sealed class RenderItem<T> {

        @Immutable
        class RenderPathItem<T>(
            val shape: FloatArray,
            val paint: T,
        ) : RenderItem<T>()

        @Immutable
        class RenderPolygonItem<T>(
            val shape: FloatArray,
            val paint: T,
        ) : RenderItem<T>()

        @Immutable
        class RenderCircleItem<T>(
            val cX: Float,
            val cY: Float,
            val radius: Float,
            val paint: T,
        ) : RenderItem<T>()

        @Immutable
        class RenderIconItem<T>(
            val cX: Float,
            val cY: Float,
            @DrawableRes val iconRes: Int,
        ) : RenderItem<T>()
    }

    class WorldData(
        val bounds: RectD = RectD(),
        val objectList: List<MapItem> = emptyList(),
    )

    class UserData(
        var userPosition: PointD? = null,
        var compass: Float = 0f,
        var objectList: List<MapItem> = emptyList(),
    )
}
