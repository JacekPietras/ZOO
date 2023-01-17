package com.jacekpietras.mapview.utils

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.jacekpietras.geometry.PointD

fun pointsToFloatArray(list: List<PointD>): FloatArray {
    val result = FloatArray(list.size * 2)
    for (i in list.indices) {
        result[i shl 1] = list[i].x.toFloat()
        result[(i shl 1) + 1] = list[i].y.toFloat()
    }
    return result
}

fun doAnimation(onUpdate: (progress: Float) -> Unit) {
    ValueAnimator.ofFloat(1f)
        .apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                onUpdate(animation.animatedFraction)
            }
            start()
        }
}
