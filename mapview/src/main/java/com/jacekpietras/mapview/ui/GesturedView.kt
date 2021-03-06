package com.jacekpietras.mapview.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.jacekpietras.mapview.utils.RotationGestureDetector

abstract class GesturedView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var swipeDetector = GestureDetector(context, OnGestureListener())
    private var pinchDetector = ScaleGestureDetector(context, OnScaleGestureListener())
    private var rotateDetector = RotationGestureDetector(OnRotationGestureListener())

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        pinchDetector.onTouchEvent(event)
        swipeDetector.onTouchEvent(event)
        rotateDetector.onTouchEvent(event)
        return true
    }

    abstract fun onScaleBegin(x: Float, y: Float)

    abstract fun onScale(scale: Float)

    abstract fun onRotate(rotate: Float)

    abstract fun onRotateBegin()

    abstract fun onScroll(vX: Float, vY: Float)

    abstract fun onClick(x: Float, y: Float)

    private inner class OnGestureListener : GestureDetector.OnGestureListener {

        override fun onShowPress(e: MotionEvent?) = Unit

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick(e.x, e.y)
            return false
        }

        override fun onDown(e: MotionEvent?) = false

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, vX: Float, vY: Float) = false

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, vX: Float, vY: Float): Boolean {
            onScroll(vX, vY)
            return false
        }

        override fun onLongPress(e: MotionEvent?) = Unit
    }

    private inner class OnScaleGestureListener : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            onScaleBegin(detector?.focusX ?: 0f, detector?.focusY ?: 0f)
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) = Unit

        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            onScale(detector?.scaleFactor ?: 1f)
            return false
        }
    }

    private inner class OnRotationGestureListener :
        RotationGestureDetector.OnRotationGestureListener {

        override fun onRotation(rotationDetector: RotationGestureDetector?) {
            onRotate(rotationDetector?.angle ?: 0f)
        }

        override fun onRotationStart(rotationDetector: RotationGestureDetector?) {
            onRotateBegin()
        }
    }
}