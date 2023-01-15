package com.jacekpietras.mapview.utils

import android.graphics.Color
import android.opengl.GLES20

internal fun setOpenGlClearColor(color: Int) {
    val red = Color.red(color).toFloat() / 255
    val green = Color.green(color).toFloat() / 255
    val blue = Color.blue(color).toFloat() / 255
    val alpha = Color.alpha(color).toFloat() / 255
    GLES20.glClearColor(red, green, blue, alpha)
}