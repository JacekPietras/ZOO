package com.jacekpietras.mapview.model


sealed class OpenGLPaint {

    abstract val color: FloatArray
    abstract val alpha: Float

    class Stroke(
        override val color: FloatArray,
        override val alpha: Float = 1f,
        val width: Float = 0.0f,
        val dashed: Boolean = false,
    ) : OpenGLPaint()

    class Fill(
        override val color: FloatArray,
        override val alpha: Float = 1f,
    ) : OpenGLPaint()

    class Circle(
        override val color: FloatArray,
        override val alpha: Float = 1f,
    ) : OpenGLPaint()
}