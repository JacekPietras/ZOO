package com.jacekpietras.mapview.utils

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.jacekpietras.core.PointD

fun pointsToDoubleArray(list: List<PointD>): DoubleArray {
    val result = DoubleArray(list.size * 2)
    for (i in list.indices) {
        result[i shl 1] = list[i].x
        result[(i shl 1) + 1] = list[i].y
    }
    return result
}

fun View.doAnimation(onUpdate: (progress: Float) -> Unit) {
    ValueAnimator.ofFloat(1f)
        .apply {
            duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                onUpdate(animation.animatedFraction)
            }
            start()
        }
}
