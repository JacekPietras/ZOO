package com.jacekpietras.mapview.ui.opengl

import com.jacekpietras.mapview.utils.colorToGLFloatArray
import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal abstract class ShapeData(color: Int) {
    val color = color.colorToGLFloatArray()
    abstract val vertexCount: Int
    abstract val vertexBuffer: FloatBuffer
}

internal abstract class OrganizedShapeData(color: Int) : ShapeData(color) {
    abstract val drawListBuffer: ShortBuffer
}
