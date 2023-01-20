package com.jacekpietras.mapview.utils

import android.graphics.Color
import android.opengl.GLES20

const val GL_MATRIX_VAR = "uMVPMatrix"
const val GL_POSITION_VAR = "vPosition"
const val GL_COLOR_VAR = "vPosition"

internal fun setOpenGLClearColor(color: Int) {
    val red = Color.red(color).toFloat() / 255
    val green = Color.green(color).toFloat() / 255
    val blue = Color.blue(color).toFloat() / 255
    val alpha = Color.alpha(color).toFloat() / 255
    GLES20.glClearColor(red, green, blue, alpha)
}

internal fun Int.colorToGLFloatArray(): FloatArray {
    val red = Color.red(this).toFloat() / 255
    val green = Color.green(this).toFloat() / 255
    val blue = Color.blue(this).toFloat() / 255
    val alpha = Color.alpha(this).toFloat() / 255
    return floatArrayOf(red, green, blue, alpha)
}

internal fun createProgram(): Int {
    val vertexShaderCode =
    // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 $GL_MATRIX_VAR;" +
                "attribute vec4 $GL_POSITION_VAR;" +
                "void main() {" +
                // the matrix must be included as a modifier of gl_Position
                // Note that the uMVPMatrix factor *must be first* in order
                // for the matrix multiplication product to be correct.
                "  gl_Position = $GL_MATRIX_VAR * $GL_POSITION_VAR;" +
                "}"

    val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 $GL_COLOR_VAR;" +
                "void main() {" +
                "  gl_FragColor = $GL_COLOR_VAR;" +
                "}"

    val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
    val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
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

internal fun Int.setGLColor(color: FloatArray) {
    // get handle to fragment shader's vColor member
    val mColorHandle = GLES20.glGetUniformLocation(this, GL_COLOR_VAR)
    // Set color for drawing the triangle
    GLES20.glUniform4fv(mColorHandle, 1, color, 0)
}
