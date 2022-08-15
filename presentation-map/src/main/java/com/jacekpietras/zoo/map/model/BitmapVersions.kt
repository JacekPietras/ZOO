package com.jacekpietras.zoo.map.model

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

class BitmapVersions(
    context: Context,
    dayRes: Int,
    nightRes: Int,
) {

    val bitmapDay = ContextCompat.getDrawable(context, dayRes)?.toBitmap()
    val bitmapNight = ContextCompat.getDrawable(context, nightRes)?.toBitmap()

    fun recycle() {
        bitmapDay?.recycle()
        bitmapNight?.recycle()
    }
}