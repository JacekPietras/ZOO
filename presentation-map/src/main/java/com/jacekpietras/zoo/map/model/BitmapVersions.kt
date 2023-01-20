package com.jacekpietras.zoo.map.model

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

class BitmapVersions(
    context: Context,
    res: Int,
    nightRes: Int? = null,
) {

    private val bitmapDay = ContextCompat.getDrawable(context, res)?.toBitmap()
    private val bitmapNight = nightRes?.let { ContextCompat.getDrawable(context, it) }?.toBitmap()

    fun get(nightTheme: Boolean = false): Bitmap? =
        if (nightTheme) {
            bitmapNight ?: bitmapDay
        } else {
            bitmapDay ?: bitmapNight
        }

    fun recycle() {
        bitmapDay?.recycle()
        bitmapNight?.recycle()
    }
}