package com.jacekpietras.zoo.map

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import androidx.core.graphics.contains
import androidx.core.graphics.plus
import androidx.core.graphics.toPointF
import kotlin.math.abs
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

        Log.i("dupa", System.currentTimeMillis().toString() + "     " + renderList.map { shape ->
            when (shape) {
                is RectF -> "RectF"
                is PointF -> "PointF"
                is Rect -> "Rect"
                is Point -> "Point"
                is PathF -> "PathF"
                is DashedPathF -> "DashedPathF"
                else -> "Unknown"
            }
        }.toString())
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
        this.list.zipWithNext().any { visibleGpsCoordinate.containsLine(it.first, it.second) }

    private fun DashedPathF.isVisible(): Boolean =
        this.list.zipWithNext().any { visibleGpsCoordinate.containsLine(it.first, it.second) }

    private fun RectF.containsLine(
        p1: PointF,
        p2: PointF
    ): Boolean {
        // Find min and max X for the segment
        var minX = p1.x
        var maxX = p2.x
        if (p1.x > p2.x) {
            minX = p2.x
            maxX = p1.x
        }

        // Find the intersection of the segment's and rectangle's x-projections
        if (maxX > right) maxX = right
        if (minX < left) minX = left

        // If their projections do not intersect return false
        if (minX > maxX) return false

        // Find corresponding min and max Y for min and max X we found before
        var minY = p1.y
        var maxY = p2.y
        val dx = p2.x - p1.x
        if (abs(dx) > 0.0000001) {
            val a = (p2.y - p1.y) / dx
            val b = p1.y - a * p1.x
            minY = a * minX + b
            maxY = a * maxX + b
        }
        if (minY > maxY) {
            val tmp = maxY
            maxY = minY
            minY = tmp
        }

        // Find the intersection of the segment's and rectangle's y-projections
        if (maxY > bottom) maxY = bottom
        if (minY < top) minY = top

        return minY <= maxY
    }

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