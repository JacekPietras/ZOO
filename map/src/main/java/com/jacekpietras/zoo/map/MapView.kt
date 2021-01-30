package com.jacekpietras.zoo.map

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import androidx.core.graphics.contains
import androidx.core.graphics.plus
import androidx.core.graphics.toPointF
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

    private lateinit var visibleGpsCoordinate: RectF
    private var horizontalScale: Float = 1f
    private var verticalScale: Float = 1f
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
            26f to 30f
        )
    )
    private lateinit var renderList: List<Any>

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calcVisibleCoordinates()
    }

    override fun onScale(scale: Float) {
        zoom = zoom.div(sqrt(scale)).coerceAtMost(10f).coerceAtLeast(2f)
        calcVisibleCoordinates()
        invalidate()
    }

    override fun onScroll(vX: Float, vY: Float) {
        val horizontalScale = width / visibleGpsCoordinate.width()
        val verticalScale = height / visibleGpsCoordinate.height()
        centerGpsCoordinate += PointF(vX / horizontalScale, vY / verticalScale)
        calcVisibleCoordinates()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        renderList.forEach { shape ->
            when (shape) {
                is Rect -> canvas.drawRect(shape, paint)
                is PathF -> canvas.drawPath(shape, strokePaint)
                is DashedPathF -> canvas.drawPath(shape, dashedPaint)
                else -> throw IllegalStateException("Unknown shape type $shape")
            }
        }
    }

    private fun calcVisibleCoordinates() {
        val ratioZoom = zoom * (height / width.toFloat())
        visibleGpsCoordinate = RectF(
            centerGpsCoordinate.x - zoom,
            centerGpsCoordinate.y - ratioZoom,
            centerGpsCoordinate.x + zoom,
            centerGpsCoordinate.y + ratioZoom
        )

        horizontalScale = width / visibleGpsCoordinate.width()
        verticalScale = height / visibleGpsCoordinate.height()

        renderList = objectList
            .asSequence()
            .filter { shape ->
                when (shape) {
                    is RectF -> shape.isVisible()
                    is PointF -> shape.isVisible()
                    is PathF -> shape.isVisible()
                    is DashedPathF -> shape.isVisible()
                    else -> throw IllegalStateException("Unknown shape type $shape")
                }
            }
            .map { shape ->
                @Suppress("IMPLICIT_CAST_TO_ANY")
                when (shape) {
                    is RectF -> shape.toViewCoordinates()
                    is PointF -> shape.toViewCoordinates()
                    is PathF -> shape.toViewCoordinates()
                    is DashedPathF -> shape.toViewCoordinates()
                    else -> throw IllegalStateException("Unknown shape type $shape")
                }
            }
            .toList()

        Log.i("dupa", renderList.size.toString())
    }

    private fun RectF.toViewCoordinates(): Rect =
        Rect(
            ((left - visibleGpsCoordinate.left) * horizontalScale).toInt(),
            ((top - visibleGpsCoordinate.top) * verticalScale).toInt(),
            ((right - visibleGpsCoordinate.left) * horizontalScale).toInt(),
            ((bottom - visibleGpsCoordinate.top) * verticalScale).toInt()
        )

    private fun PointF.toViewCoordinates(): Point =
        Point(
            ((x - visibleGpsCoordinate.left) * horizontalScale).toInt(),
            ((y - visibleGpsCoordinate.top) * verticalScale).toInt()
        )

    private fun PathF.toViewCoordinates(): PathF =
        PathF(this.list.map { it.toViewCoordinates().toPointF() })

    private fun DashedPathF.toViewCoordinates(): DashedPathF =
        DashedPathF(this.list.map { it.toViewCoordinates().toPointF() })

    private fun RectF.isVisible(): Boolean =
        visibleGpsCoordinate.intersects(left, top, right, bottom)

    private fun PointF.isVisible(): Boolean =
        visibleGpsCoordinate.contains(this)

    private fun PathF.isVisible(): Boolean =
        this.list.any { visibleGpsCoordinate.contains(it) }

    private fun DashedPathF.isVisible(): Boolean =
        this.list.any { visibleGpsCoordinate.contains(it) }

    private val Int.dp: Float
        get() =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                toFloat(),
                context.resources.displayMetrics
            )

    class PathF(val list: List<PointF>) {

        constructor(vararg points: PointF) : this(points.asList())

        constructor(vararg points: Pair<Float, Float>)
                : this(points.map { PointF(it.first, it.second) })
    }

    class DashedPathF(val list: List<PointF>) {

        constructor(vararg points: PointF) : this(points.asList())

        constructor(vararg points: Pair<Float, Float>)
                : this(points.map { PointF(it.first, it.second) })
    }

    private fun Canvas.drawPath(path: PathF, paint: Paint) {
        val toDraw = Path()
        path.list.forEachIndexed { i, point ->
            if (i == 0) {
                toDraw.moveTo(point.x, point.y)
            } else {
                toDraw.lineTo(point.x, point.y)
            }
        }
        drawPath(toDraw, paint)
    }

    private fun Canvas.drawPath(path: DashedPathF, paint: Paint) {
        val toDraw = Path()
        path.list.forEachIndexed { i, point ->
            if (i == 0) {
                toDraw.moveTo(point.x, point.y)
            } else {
                toDraw.lineTo(point.x, point.y)
            }
        }
        drawPath(toDraw, paint)
    }
}