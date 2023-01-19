package com.jacekpietras.mapview.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.ui.LastMapUpdate.cutoE
import com.jacekpietras.mapview.ui.LastMapUpdate.cutoS
import com.jacekpietras.mapview.ui.LastMapUpdate.mergE
import com.jacekpietras.mapview.ui.LastMapUpdate.moveE
import com.jacekpietras.mapview.ui.LastMapUpdate.rendE
import com.jacekpietras.mapview.ui.LastMapUpdate.rendS
import com.jacekpietras.mapview.ui.LastMapUpdate.sortE
import com.jacekpietras.mapview.ui.LastMapUpdate.sortS
import com.jacekpietras.mapview.ui.LastMapUpdate.trans
import com.jacekpietras.mapview.utils.ViewGestures
import com.jacekpietras.mapview.utils.drawMapObjects
import timber.log.Timber

class MapSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr) {

    var onSizeChanged: ((width: Int, height: Int) -> Unit)? = null
    var onClick: ((Float, Float) -> Unit)? = null
    var onTransform: ((Float, Float, Float, Float, Float, Float) -> Unit)? = null
    var mapList: List<RenderItem<Paint>> = emptyList()
        set(value) {
            field = value
            invalidate()

            val prevRendE = rendE
            rendE = System.nanoTime()
            if (trans > 0) {
                Timber.d(
                    "Perf: Render: Full: ${trans toMs rendE}, from prev ${prevRendE toMs rendE}\n" +
                            "    [pass to vm] ${trans toMs cutoS}\n" +
                            "    [coord prep] ${cutoS toMs moveE}\n" +
                            "    [ translate] ${moveE toMs sortS}\n" +
                            "    [      sort] ${sortS toMs sortE}\n" +
                            "    [       sum] ${sortE toMs mergE}\n" +
                            "    [invali req] ${mergE toMs cutoE}\n" +
                            "    [invalidate] ${cutoE toMs rendS}\n" +
                            "    [    render] ${rendS toMs rendE}"
                )
            }
        }

    private infix fun Long.toMs(right: Long) =
        "${(right - this) / 10_000 / 1_00.0} ms"

    private val viewGestures = object : ViewGestures(context) {

        override fun onTransform(cX: Float, cY: Float, scale: Float, rotate: Float, vX: Float, vY: Float) {
            onTransform?.invoke(cX, cY, scale, rotate, vX, vY)
        }

        override fun onClick(x: Float, y: Float) {
            onClick?.invoke(x, y)
        }
    }

    init {
        onSizeChanged?.invoke(width, height)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onSizeChanged?.invoke(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        onSizeChanged?.invoke(width, height)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        viewGestures.onTouchEvent(event)
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawMapObjects(mapList)
    }
}
