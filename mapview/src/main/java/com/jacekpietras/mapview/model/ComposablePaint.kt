package com.jacekpietras.mapview.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke.Companion.DefaultMiter

sealed class ComposablePaint {

    abstract val color: Color
    abstract val alpha: Float

    class Stroke(
        override val color: Color = Color.Red,
        override val alpha: Float = 1f,
        width: Float = 0.0f,
        miter: Float = DefaultMiter,
        cap: StrokeCap = StrokeCap.Butt,
        join: StrokeJoin = StrokeJoin.Miter,
        pathEffect: PathEffect? = null,
    ) : ComposablePaint() {

        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
            width = width,
            miter = miter,
            cap = cap,
            join = join,
            pathEffect = pathEffect,
        )
    }

    class Fill(
        override val color: Color = Color.Red,
        override val alpha: Float = 1f,
    ) : ComposablePaint()
}