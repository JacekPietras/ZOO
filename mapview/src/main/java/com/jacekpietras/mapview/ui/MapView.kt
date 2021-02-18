package com.jacekpietras.mapview.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.android.material.color.MaterialColors
import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.core.polygonContains
import com.jacekpietras.mapview.BuildConfig
import com.jacekpietras.mapview.R
import com.jacekpietras.mapview.model.*
import com.jacekpietras.mapview.utils.drawPath
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class MapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GesturedView(context, attrs, defStyleAttr) {

    var maxZoom: Double = 10.0
    var minZoom: Double = 2.0
    var worldBounds: RectD = RectD()
        set(value) {
            field = value
            centerGpsCoordinate = PointD(value.centerX(), value.centerY())
            maxZoom = min(abs(value.width()), abs(value.height())) / 2
            minZoom = maxZoom / 8
            zoom = maxZoom / 4
        }
    private var _objectList: List<RenderItem> = emptyList()
    var objectList: List<MapItem> = emptyList()
        set(value) {
            Timber.v("Content changed")
            field = value
            _objectList = value.toRenderItems()
            cutOutNotVisible()
            invalidate()
        }

    var userPosition: PointD? = null
        set(value) {
            Timber.v("Position changed ${value?.x}")
            field = value
            if (centeringAtUser) {
                centerAtUserPosition()
            }
        }
    var terminalPoints: List<PointD> = emptyList()
        set(value) {
            field = value
            cutOutNotVisible()
            invalidate()
        }
    var terminalPointsOnScreen: FloatArray? = null
    var userPositionOnScreen: FloatArray? = null
    var compass: Float = 0f
        set(value) {
            field = value
            if (centeringAtUser) {
                centerAtUserCompass()
            }
        }

    private lateinit var visibleGpsCoordinate: ViewCoordinates
    private var centerGpsCoordinate: PointD =
        PointD(worldBounds.centerX(), worldBounds.centerY())
    private var zoom: Double = 5.0
    private var zoomOnStart: Double = 5.0
    private var worldRotation: Float = 0f
    private var worldRotationOnStart: Float = 0f
    private var renderList: List<RenderItem2>? = null
    private var centeringAtUser = false
    private val debugTextPaint = Paint()
        .apply {
            color = MaterialColors.getColor(context, R.attr.colorControlActivated, Color.MAGENTA)
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            alpha = 128
            textSize = 30f
        }
    private val userPositionPaint = Paint()
        .apply {
            color = MapColor.Attribute(R.attr.colorPrimary).toColorInt(context)
            style = Paint.Style.FILL
        }
    private val terminalPaint = Paint()
        .apply {
            color = Color.RED
            alpha = 128
            style = Paint.Style.FILL
        }

    fun centerAtUserPosition(animation: Boolean = true) {
        centeringAtUser = true
        val desiredPosition = userPosition ?: return

        if (animation) {
            val previousPosition = centerGpsCoordinate
            ValueAnimator.ofFloat(1f)
                .apply {
                    duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { animation ->
                        centerGpsCoordinate = previousPosition * (1f - animation.animatedFraction) +
                                desiredPosition * animation.animatedFraction

                        cutOutNotVisible()
                        invalidate()
                    }
                    start()
                }
        } else {
            centerGpsCoordinate = desiredPosition
            cutOutNotVisible()
            invalidate()
        }
    }

    private fun centerAtUserCompass(animation: Boolean = true) {
        if (animation) {
            val diff = worldRotation - compass
            val previousPosition = when {
                diff > 180 -> worldRotation - 360
                diff < -180 -> worldRotation + 360
                else -> worldRotation
            }

            ValueAnimator.ofFloat(1f)
                .apply {
                    duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { animation ->
                        worldRotation = (previousPosition * (1f - animation.animatedFraction) +
                                compass * animation.animatedFraction) % 360f

                        cutOutNotVisible()
                        invalidate()
                    }
                    start()
                }
        } else {
            worldRotation = compass
            cutOutNotVisible()
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cutOutNotVisible()
    }

    override fun onScaleBegin(x: Float, y: Float) {
        zoomOnStart = zoom
    }

    override fun onScale(scale: Float) {
        val zoomPoint = (maxZoom - minZoom) / 2
        zoom = (zoomOnStart + zoomPoint * (1 - scale)).coerceAtMost(maxZoom).coerceAtLeast(minZoom)
        cutOutNotVisible()
        invalidate()
    }

    override fun onRotateBegin() {
        worldRotationOnStart = worldRotation
    }

    override fun onRotate(rotate: Float) {
        worldRotation = rotate + worldRotationOnStart
    }

    override fun onScroll(vX: Float, vY: Float) {
        centeringAtUser = false
        val radians = Math.toRadians(-worldRotation.toDouble())
        centerGpsCoordinate += PointD(
            (sin(radians) * vY + cos(radians) * vX) / visibleGpsCoordinate.horizontalScale,
            (-sin(radians) * vX + cos(radians) * vY) / visibleGpsCoordinate.verticalScale
        )
        cutOutNotVisible()
        invalidate()
    }

    override fun onClick(x: Float, y: Float) {
        renderList?.forEach { item ->
            item.onClick?.let {
                if (polygonContains(item.shape, x, y)) {
                    it.invoke(x, y)
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        renderList?.forEach { canvas.drawPath(it.shape, it.paint, it.close) }
        userPositionOnScreen?.let {
            canvas.drawCircle(it[0], it[1], 15f, userPositionPaint)
        }
        terminalPointsOnScreen?.let { array ->
            for (i in array.indices step 2) {
                canvas.drawCircle(array[i], array[i + 1], 15f, terminalPaint)
            }
        }

        renderDebug(canvas)
    }

    private fun renderDebug(canvas: Canvas) {
        if (BuildConfig.DEBUG && ::visibleGpsCoordinate.isInitialized) {
            canvas.drawText("wrld: " + worldBounds.toShortString(), 10f, 40f, debugTextPaint)
            canvas.drawText(
                "curr: ${visibleGpsCoordinate.visibleRect.toShortString()}",
                10f,
                80f,
                debugTextPaint
            )

            canvas.drawText(
                "zoom: ${((zoom - minZoom) / (maxZoom - minZoom)).form()}",
                10f,
                120f,
                debugTextPaint
            )
            canvas.drawText(
                "rot:  [${worldRotation.toDouble().form()}]",
                10f,
                160f,
                debugTextPaint
            )
            userPosition?.let {
                canvas.drawText(
                    "upos: [${it.x.form()},${it.y.form()}]",
                    10f,
                    200f,
                    debugTextPaint
                )
            }
        }
    }

    private fun Double.form() = "%.6f".format(this)

    private fun List<MapItem>.toRenderItems(): List<RenderItem> {
        val innerPaints = mutableMapOf<MapPaint, PaintHolder>()
        val borderPaints = mutableMapOf<MapPaint, PaintHolder?>()

        return map { item ->
            val inner = innerPaints[item.paint]
                ?: item.paint.toCanvasPaint(context)
                    .also { innerPaints[item.paint] = it }
            val border = borderPaints[item.paint]
                ?: item.paint.toBorderCanvasPaint(context)
                    .also { borderPaints[item.paint] = it }

            when (item.shape) {
                is PathD -> RenderItem(
                    pointsToDoubleArray(item.shape.vertices),
                    inner,
                    border,
                    close = false,
                )
                is PolygonD -> RenderItem(
                    pointsToDoubleArray(item.shape.vertices),
                    inner,
                    border,
                    item.onClick,
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

    private fun PaintHolder.takePaint(dynamicPaints: MutableMap<PaintHolder.Dynamic, Paint>): Paint {
        return when (this) {
            is PaintHolder.Static -> paint
            is PaintHolder.Dynamic -> {
                dynamicPaints[this]
                    ?: block(zoom, centerGpsCoordinate, width)
                        .also { dynamicPaints[this] = it }
            }
        }
    }

    private fun cutOutNotVisible() {
        if (width == 0 || height == 0) return
        if (worldBounds.width() == 0.0 || worldBounds.height() == 0.0) return

        visibleGpsCoordinate = ViewCoordinates(centerGpsCoordinate, zoom, width, height)

        if (preventedGoingOutsideWorld()) {
            cutOutNotVisible()
            return
        }

        val borders = mutableListOf<RenderItem2>()
        val insides = mutableListOf<RenderItem2>()
        val dynamicPaints = mutableMapOf<PaintHolder.Dynamic, Paint>()

        val matrix = Matrix()
            .apply {
                setRotate(
                    -worldRotation,
                    width / 2.toFloat(),
                    height / 2.toFloat(),
                )
            }

        _objectList.forEach { item ->
            if (item.close) {
                visibleGpsCoordinate
                    .transformPolygon(item.shape)
                    ?.withMatrix(matrix, worldRotation)
                    ?.let { polygon ->
                        item.addToRender2(polygon, borders, insides, dynamicPaints)
                    }
            } else {
                visibleGpsCoordinate
                    .transformPath(item.shape)
                    .map { it.withMatrix(matrix, worldRotation) }
                    .forEach { path ->
                        item.addToRender2(path, borders, insides, dynamicPaints)
                    }
            }
        }
        userPosition?.let {
            userPositionOnScreen = visibleGpsCoordinate
                .transformPoint(it)
                .withMatrix(matrix, worldRotation)
        }
        if (terminalPoints.isNotEmpty()) {
            terminalPointsOnScreen = visibleGpsCoordinate
                .transformPoints(terminalPoints)
                .withMatrix(matrix, worldRotation)
        }

        renderList = borders + insides
        logVisibleShapes()
    }

    private fun FloatArray.withMatrix(matrix: Matrix, worldRotation: Float): FloatArray {
        if (worldRotation != 0f) {
            matrix.mapPoints(this)
        }
        return this
    }

    private fun RenderItem.addToRender2(
        array: FloatArray,
        borders: MutableList<RenderItem2>,
        insides: MutableList<RenderItem2>,
        dynamicPaints: MutableMap<PaintHolder.Dynamic, Paint>,
    ) {
        insides.add(
            RenderItem2(
                array,
                paintHolder.takePaint(dynamicPaints),
                onClick,
                close
            )
        )
        if (outerPaintHolder != null) {
            borders.add(
                RenderItem2(
                    array,
                    outerPaintHolder.takePaint(dynamicPaints),
                    onClick,
                    close
                )
            )
        }
    }

    private fun logVisibleShapes() {
        if (BuildConfig.DEBUG) {
            val message = "Preparing render list: " +
                    renderList?.map { "Shape " + (it.shape.size shr 1) } +
                    " (${objectList.size})"
            Timber.v(message)
        }
    }

    private fun pointsToDoubleArray(list: List<PointD>): DoubleArray {
        val result = DoubleArray(list.size * 2)
        for (i in list.indices) {
            result[i shl 1] = list[i].x
            result[(i shl 1) + 1] = list[i].y
        }
        return result
    }

    private class RenderItem(
        val shape: DoubleArray,
        val paintHolder: PaintHolder,
        val outerPaintHolder: PaintHolder? = null,
        val onClick: ((x: Float, y: Float) -> Unit)? = null,
        val close: Boolean,
    )

    private class RenderItem2(
        val shape: FloatArray,
        val paint: Paint,
        val onClick: ((x: Float, y: Float) -> Unit)? = null,
        val close: Boolean,
    )
}


