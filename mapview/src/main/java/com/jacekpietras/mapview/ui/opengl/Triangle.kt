package com.jacekpietras.mapview.ui.opengl

import android.graphics.Color
import android.opengl.GLES20
import com.jacekpietras.mapview.utils.BYTES_PER_FLOAT
import com.jacekpietras.mapview.utils.GL_COLOR_VAR
import com.jacekpietras.mapview.utils.GL_MATRIX_VAR
import com.jacekpietras.mapview.utils.GL_POSITION_VAR
import com.jacekpietras.mapview.utils.allocateFloatBuffer
import com.jacekpietras.mapview.utils.colorToGLFloatArray
import com.jacekpietras.mapview.utils.createProgram

private const val COORDS_PER_VERTEX = 3
private val triangleCoords = floatArrayOf(
    0f, 0f, 0f,
    0f, 2340f, 0.0f,
    1080f, 2340f, 0.0f,
)
private const val VERTEX_COUNT = 3

class Triangle {

    private val color = Color.RED.colorToGLFloatArray()
    private val mProgram = createProgram()
    private val vertexBuffer = allocateFloatBuffer(triangleCoords)

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(mProgram)

        val vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, GL_MATRIX_VAR)
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

        val mPositionHandle = GLES20.glGetAttribLocation(mProgram, GL_POSITION_VAR)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            COORDS_PER_VERTEX * BYTES_PER_FLOAT,
            vertexBuffer
        )

        val mColorHandle = GLES20.glGetUniformLocation(mProgram, GL_COLOR_VAR)
        GLES20.glUniform4fv(mColorHandle, 1, color, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTEX_COUNT)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisable(mColorHandle)
    }
}
