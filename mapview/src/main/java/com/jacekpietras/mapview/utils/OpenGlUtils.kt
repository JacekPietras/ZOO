package com.jacekpietras.mapview.utils

import android.graphics.Color.alpha
import android.graphics.Color.blue
import android.graphics.Color.green
import android.graphics.Color.red
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal const val GL_MATRIX_VAR = "uMVPMatrix"
internal const val GL_POSITION_VAR = "vPosition"
internal const val GL_COLOR_VAR = "vPosition"

internal const val COORDS_PER_VERTEX = 3

internal const val BYTES_PER_FLOAT = 4
internal const val BYTES_PER_SHORT = 2

internal fun setOpenGLClearColor(color: Int) {
    val red = red(color) / 255f
    val green = green(color) / 255f
    val blue = blue(color) / 255f
    val alpha = alpha(color) / 255f
    GLES20.glClearColor(red, green, blue, alpha)
}

internal fun Int.colorToGLFloatArray(): FloatArray {
    val red = red(this) / 255f
    val green = green(this) / 255f
    val blue = blue(this) / 255f
    val alpha = alpha(this) / 255f
    return floatArrayOf(red, green, blue, alpha)
}

private const val VERTEX_SHADER_CODE =
    "uniform mat4 $GL_MATRIX_VAR;" +
            "attribute vec4 $GL_POSITION_VAR;" +
            "void main() {" +
            "  gl_Position = $GL_MATRIX_VAR * $GL_POSITION_VAR;" +
            "}"

private const val FRAGMENT_SHADER_CODE =
    "precision mediump float;" +
            "uniform vec4 $GL_COLOR_VAR;" +
            "void main() {" +
            "  gl_FragColor = $GL_COLOR_VAR;" +
            "}"

internal fun createGLProgram(): Int {
    val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
    val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
    return GLES20.glCreateProgram().also {
        GLES20.glAttachShader(it, vertexShader)
        GLES20.glAttachShader(it, fragmentShader)
        GLES20.glLinkProgram(it)
    }
}

private fun loadShader(type: Int, shaderCode: String): Int =
    GLES20.glCreateShader(type).also { shader ->
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
    }

internal fun allocateFloatBuffer(array: FloatArray): FloatBuffer =
    ByteBuffer.allocateDirect(array.size * BYTES_PER_FLOAT).run {
        // use the device hardware's native byte order
        order(ByteOrder.nativeOrder())

        // create a floating point buffer from the ByteBuffer
        asFloatBuffer().apply {
            // add the coordinates to the FloatBuffer
            put(array)
            // set the buffer to read the first coordinate
            position(0)
        }
    }

internal fun allocateShortBuffer(array: ShortArray): ShortBuffer =
    ByteBuffer.allocateDirect(array.size * BYTES_PER_SHORT).run {
        // use the device hardware's native byte order
        order(ByteOrder.nativeOrder())

        // create a floating point buffer from the ByteBuffer
        asShortBuffer().apply {
            // add the coordinates to the FloatBuffer
            put(array)
            // set the buffer to read the first coordinate
            position(0)
        }
    }

internal fun FloatArray.addZDimension(): FloatArray {
    var srcI = 0
    return FloatArray(size / 2 * COORDS_PER_VERTEX) { resIt ->
        if (resIt % COORDS_PER_VERTEX != 2) {
            this[srcI++]
        } else {
            0f
        }
    }
}

internal fun createCircularStamp(points: Int): FloatArray {
    fun angle(i: Int): Double =
        2 * PI * (i / COORDS_PER_VERTEX) / points

    return FloatArray((points + 1) * COORDS_PER_VERTEX) {
        if (it < COORDS_PER_VERTEX) {
            // center point
            0f
        } else {
            // on border
            when (it % COORDS_PER_VERTEX) {
                0 -> sin(angle(it - COORDS_PER_VERTEX)).toFloat()
                1 -> cos(angle(it - COORDS_PER_VERTEX)).toFloat()
                else -> 0f
            }
        }
    }
}

internal fun createCircularIndicesStamp(points: Int): ShortArray {
    return ShortArray(points * COORDS_PER_VERTEX) {
        if (it == points * COORDS_PER_VERTEX - 1) {
            1
        } else {
            when (it % COORDS_PER_VERTEX) {
                0 -> 0
                1 -> (it / 3 + 1).toShort()
                else -> (it / 3 + 2).toShort()
            }
        }
    }
}
