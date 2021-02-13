package com.jacekpietras.zoo.map.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import com.jacekpietras.zoo.domain.model.PointD
import com.jacekpietras.zoo.domain.model.RectD
import com.jacekpietras.zoo.map.BuildConfig
import com.jacekpietras.zoo.map.model.*
import com.jacekpietras.zoo.map.utils.drawPath
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.min


internal class MapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GesturedView(context, attrs, defStyleAttr) {
    //  [19.940416, 50.083510] [19.948745, 50.075829]

    var maxZoom: Double = 10.0
    var minZoom: Double = 2.0
    var worldBounds: RectD = RectD()
        set(value) {
            field = value
            centerGpsCoordinate = PointD(value.centerX(), value.centerY())
            maxZoom = min(abs(value.width()), abs(value.height())) / 2
            minZoom = maxZoom / 6
            zoom = maxZoom / 3
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
    var userPositionOnScreen: PointF? = null

    private lateinit var visibleGpsCoordinate: ViewCoordinates
    private var centerGpsCoordinate: PointD =
        PointD(worldBounds.centerX(), worldBounds.centerY())
    private var zoom: Double = 5.0
    private var zoomOnStart: Double = 5.0
    private var renderList: List<RenderItem>? = null
    private var centeringAtUser = false
    private val debugTextPaint = Paint()
        .apply {
            color = Color.parseColor("#88444444")
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textSize = 30f
        }
    private val userPositionPaint = Paint()
        .apply {
            color = Color.MAGENTA
            style = Paint.Style.FILL
        }

    fun centerAtUserPosition(animation: Boolean = true) {
        centeringAtUser = true
        val desiredPosition = userPosition ?: throw IllegalStateException("no user position")

        if (animation) {
            val previousPosition = centerGpsCoordinate
            ValueAnimator.ofFloat(1f)
                .apply {
                    duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { animation ->
                        animation.animatedFraction
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

    override fun onScroll(vX: Float, vY: Float) {
        centeringAtUser = false
        centerGpsCoordinate += PointD(
            vX / visibleGpsCoordinate.horizontalScale,
            vY / visibleGpsCoordinate.verticalScale
        )
        cutOutNotVisible()
        invalidate()
    }

    override fun onClick(x: Float, y: Float) {
        val point = PointF(x, y)
        renderList?.forEach { item ->
            item.onClick?.let {
                when (item.shape) {
                    is PolygonF -> if (item.shape.contains(point)) {
                        it.invoke(x, y)
                    }
                    else -> throw IllegalStateException("Unknown shape type ${item.shape}")
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        renderList?.forEach { item ->
            when (item.shape) {
                is PathF -> canvas.drawPath(item.shape, item.paint)
                is PolygonF -> canvas.drawPath(item.shape, item.paint)
                else -> throw IllegalStateException("Unknown shape type ${item.shape}")
            }
        }
        userPositionOnScreen?.let {
            canvas.drawCircle(it.x, it.y, 15f, userPositionPaint)
        }

        renderDebug(canvas)
    }

    private fun renderDebug(canvas: Canvas) {
        if (BuildConfig.DEBUG) {
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
            userPosition?.let {
                canvas.drawText(
                    "upos: [${it.x.form()},${it.y.form()}]",
                    10f,
                    160f,
                    debugTextPaint
                )
            }
        }
    }

    private fun Double.form() = "%.6f".format(this)

    private fun List<MapItem>.toRenderItems(): List<RenderItem> = map { item ->
        when (item.shape) {
            is PathD -> RenderItem(
                item.shape,
                item.paint.toCanvasPaint(context)
            )
            is PolygonD -> RenderItem(
                item.shape,
                item.paint.toCanvasPaint(context),
                item.onClick
            )
            else -> throw IllegalStateException("Unknown shape type ${item.shape}")
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

    private fun cutOutNotVisible() {
        if (width == 0 || height == 0) return

        visibleGpsCoordinate = ViewCoordinates(
            centerGpsCoordinate,
            zoom,
            width,
            height,
        )

        if (preventedGoingOutsideWorld()) {
            cutOutNotVisible()
            return
        }

        val temp = mutableListOf<RenderItem>()

        _objectList.forEach { item ->
            when (item.shape) {
                is PolygonD -> {
                    visibleGpsCoordinate.transform(item.shape)?.let {
                        temp.add(
                            RenderItem(
                                PolygonF(it.vertices.map { p -> p.toFloat() }),
                                item.paint,
                                item.onClick
                            )
                        )
                    }
                }
                is PathD -> {
                    visibleGpsCoordinate.transform(item.shape).forEach {
                        temp.add(
                            RenderItem(
                                PathF(it.vertices.map { p -> p.toFloat() }),
                                item.paint
                            )
                        )
                    }
                }
            }
        }
        userPosition?.let {
            userPositionOnScreen = visibleGpsCoordinate.transform(it).toFloat()
        }

        renderList = temp
        logVisibleShapes()
    }

    private fun logVisibleShapes() {
        if (BuildConfig.DEBUG) {
            val message = "Preparing render list: " + renderList?.map { item ->
                when (item.shape) {
                    is PathF -> "PathsF " + item.shape.vertices.size
                    is PolygonF -> "PolygonF " + item.shape.vertices.size
                    is PathD -> "PathD " + item.shape.vertices.size
                    is PolygonD -> "PolygonD " + item.shape.vertices.size
                    else -> "Unknown"
                }
            } + " (${objectList.size})"
            Timber.v(message)
        }
    }

    private class RenderItem(
        val shape: DrawableOnCanvas,
        val paint: Paint,
        val onClick: ((x: Float, y: Float) -> Unit)? = null,
    )
}