package com.jacekpietras.mapview.ui.view

import android.graphics.Color
import android.opengl.GLES20
import com.jacekpietras.mapview.utils.BYTES_PER_FLOAT
import com.jacekpietras.mapview.utils.GL_COLOR_VAR
import com.jacekpietras.mapview.utils.GL_MATRIX_VAR
import com.jacekpietras.mapview.utils.GL_POSITION_VAR
import com.jacekpietras.mapview.utils.allocateFloatBuffer
import com.jacekpietras.mapview.utils.allocateShortBuffer
import com.jacekpietras.mapview.utils.colorToGLFloatArray
import com.jacekpietras.mapview.utils.createProgram

private const val COORDS_PER_VERTEX = 3
private val pathCords = floatArrayOf(
    10.0f, 0.0f, 0.0f,
    1090f, 2340f, 0.0f
)

class Line {

    private val color = Color.BLUE.colorToGLFloatArray()
    private val mProgram = createProgram()
    private val pathDrawOrder = shortArrayOf(0, 1)
    private val vertexBuffer = allocateFloatBuffer(pathCords)
    private val drawListBuffer = allocateShortBuffer(pathDrawOrder)

    fun draw(mvpMatrix: FloatArray?) {
        GLES20.glUseProgram(mProgram)

        val mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, GL_MATRIX_VAR)
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

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

        GLES20.glDrawElements(GLES20.GL_LINES, pathDrawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisable(mColorHandle)
    }
}