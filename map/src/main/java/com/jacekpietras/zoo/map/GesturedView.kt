package com.jacekpietras.zoo.map

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

abstract class GesturedView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var swipeDetector =
        GestureDetector(context, OnGestureListener { vX, vY -> onScroll(vX, vY) })
    private var pinchDetector =
        ScaleGestureDetector(context,
            OnScaleGestureListener { onScale(it) })

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        pinchDetector.onTouchEvent(event)
        swipeDetector.onTouchEvent(event)
        return true;
    }

    abstract fun onScale(scale: Float)

    abstract fun onScroll(vX: Float, vY: Float)

    private class OnGestureListener(
        val onScroll: (Float, Float) -> Unit
    ) : GestureDetector.OnGestureListener {

        override fun onShowPress(e: MotionEvent?) = Unit

        override fun onSingleTapUp(e: MotionEvent?) = false

        override fun onDown(e: MotionEvent?) = false

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, vX: Float, vY: Float) = false

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, vX: Float, vY: Float): Boolean {
            onScroll(vX, vY)
            return false
        }

        override fun onLongPress(e: MotionEvent?) = Unit
    }

    private class OnScaleGestureListener(
        val onScale: (Float) -> Unit
    ) : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScaleBegin(detector: ScaleGestureDetector?) = true

        override fun onScaleEnd(detector: ScaleGestureDetector?) = Unit

        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            onScale(detector?.scaleFactor ?: 1f)
            return false
        }
    }
}