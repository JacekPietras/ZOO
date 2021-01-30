package com.jacekpietras.zoo.map

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import androidx.core.graphics.plus
import kotlin.math.sqrt


class MapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GesturedView(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.RED
    }
    private val dashedPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 2.dp
        pathEffect = DashPathEffect(floatArrayOf(4.dp, 8.dp), 0f)
    }
    private val strokePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 2.dp
    }

    private lateinit var visibleGpsCoordinate: ViewCoordinates
    private var centerGpsCoordinate: PointF = PointF(20f, 20f)
    private var zoom: Float = 5f
    var objectList: List<Any> = listOf(
        RectF(18f, 18f, 20f, 20f),
        RectF(28f, 15f, 40f, 25f),
        DashedPathF(
            20f to 20f,
            25f to 25f,
            25f to 30f
        ),
        PathF(
            21f to 20f,
            26f to 25f,
            26f to 30f,
            20f to 20f,
        )
    )
    private lateinit var renderList: List<Any>

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cutOutNotVisible()
    }

    override fun onScale(scale: Float) {
        zoom = zoom.div(sqrt(scale)).coerceAtMost(10f).coerceAtLeast(2f)
        cutOutNotVisible()
        invalidate()
    }

    override fun onScroll(vX: Float, vY: Float) {
        centerGpsCoordinate += PointF(
            vX / visibleGpsCoordinate.horizontalScale,
            vY / visibleGpsCoordinate.verticalScale
        )
        cutOutNotVisible()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        renderList.forEach { shape ->
            when (shape) {
                is Rect -> canvas.drawRect(shape, paint)
                is PathsF -> canvas.drawPath(shape, strokePaint)
                is DashedPathsF -> canvas.drawPath(shape, dashedPaint)
                else -> throw IllegalStateException("Unknown shape type $shape")
            }
        }
    }

    private fun cutOutNotVisible() {
        visibleGpsCoordinate = ViewCoordinates.create(centerGpsCoordinate, zoom, width, height)

        renderList = objectList
            .asSequence()
            .filter { shape ->
                when (shape) {
                    is RectF -> visibleGpsCoordinate.intersects(shape)
                    is PointF -> visibleGpsCoordinate.contains(shape)
                    is PathF -> true
                    is DashedPathF -> true
                    else -> throw IllegalStateException("Unknown shape type $shape")
                }
            }
            .map { shape ->
                @Suppress("IMPLICIT_CAST_TO_ANY")
                when (shape) {
                    is RectF -> visibleGpsCoordinate.transform(shape)
                    is PointF -> visibleGpsCoordinate.transform(shape)
                    is PathF -> visibleGpsCoordinate.transform(shape)
                    is DashedPathF -> visibleGpsCoordinate.transform(shape)
                    else -> throw IllegalStateException("Unknown shape type $shape")
                }
            }
            .filter { shape ->
                when (shape) {
                    is PathsF -> shape.list.isNotEmpty()
                    is DashedPathsF -> shape.list.isNotEmpty()
                    else -> true
                }
            }
            .toList()

        if (BuildConfig.DEBUG) {
            Log.i(
                "dupa",
                System.currentTimeMillis().toString() + "     " + renderList.map { shape ->
                    when (shape) {
                        is RectF -> "RectF"
                        is PointF -> "PointF"
                        is Rect -> "Rect"
                        is Point -> "Point"
                        is PathsF -> "PathsF " + shape.list.map { it.size }.toString()
                        is DashedPathsF -> "DashedPathsF " + shape.list.map { it.size }.toString()
                        else -> "Unknown"
                    }
                }.toString()
            )
        }
    }

    private val Int.dp: Float
        get() =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                toFloat(),
                context.resources.displayMetrics
            )
}