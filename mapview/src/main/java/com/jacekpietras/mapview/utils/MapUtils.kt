package com.jacekpietras.mapview.utils

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator

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
