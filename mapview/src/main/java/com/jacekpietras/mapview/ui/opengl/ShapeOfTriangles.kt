package com.jacekpietras.mapview.ui.opengl

import android.opengl.GLES20
import com.jacekpietras.mapview.utils.BYTES_PER_FLOAT
import com.jacekpietras.mapview.utils.COORDS_PER_VERTEX
import com.jacekpietras.mapview.utils.GL_COLOR_VAR
import com.jacekpietras.mapview.utils.GL_MATRIX_VAR
import com.jacekpietras.mapview.utils.GL_POSITION_VAR
import com.jacekpietras.mapview.utils.createGLProgram

internal open class ShapeOfTriangles {

    private val glProgram = createGLProgram()

    protected fun draw(mvpMatrix: FloatArray?, data: ShapeOfTrianglesData) {
        GLES20.glUseProgram(glProgram)

        val mMVPMatrixHandle = GLES20.glGetUniformLocation(glProgram, GL_MATRIX_VAR)
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

        val mPositionHandle = GLES20.glGetAttribLocation(glProgram, GL_POSITION_VAR)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            COORDS_PER_VERTEX * BYTES_PER_FLOAT,
            data.vertexBuffer,
        )

        val mColorHandle = GLES20.glGetUniformLocation(glProgram, GL_COLOR_VAR)
        GLES20.glUniform4fv(mColorHandle, 1, data.color, 0)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLE_FAN,
            data.drawListBuffer.capacity(),
            GLES20.GL_UNSIGNED_SHORT,
            data.drawListBuffer,
        )

        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }
}
