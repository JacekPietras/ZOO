package com.jacekpietras.mapview.utils

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.jacekpietras.core.PointD

fun Double.form() = "%.6f".format(this)

fun pointsToDoubleArray(list: List<PointD>): DoubleArray {
    val result = DoubleArray(list.size * 2)
    for (i in list.indices) {
        result[i shl 1] = list[i].x
        result[(i shl 1) + 1] = list[i].y
    }
    return result
}

fun View.doAnimation(animating: Boolean = true, onUpdate: (progress: Float, left: Float) -> Unit) {
    if (animating) {
        ValueAnimator.ofFloat(1f)
            .apply {
                duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animation ->
                    onUpdate(animation.animatedFraction, (1f - animation.animatedFraction))
                }
                start()
            }
    } else {
        onUpdate(1f, 0f)
    }
}
