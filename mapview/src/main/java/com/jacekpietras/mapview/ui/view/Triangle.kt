package com.jacekpietras.mapview.ui.view

import android.graphics.Color
import android.opengl.GLES20
import com.jacekpietras.mapview.utils.GL_MATRIX_VAR
import com.jacekpietras.mapview.utils.GL_POSITION_VAR
import com.jacekpietras.mapview.utils.colorToGLFloatArray
import com.jacekpietras.mapview.utils.createProgram
import com.jacekpietras.mapview.utils.setGLColor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

private const val COORDS_PER_VERTEX = 3
private const val BYTES_PER_FLOAT = 4
private val triangleCoords = floatArrayOf(
    0f, 0f, 0f,
    0f, 2340f, 0.0f,
    1080f, 2340f, 0.0f,
)
private const val VERTEX_COUNT = 3
private const val BYRES_PER_VERTEX = COORDS_PER_VERTEX * BYTES_PER_FLOAT

class Triangle {

    private val color = Color.RED.colorToGLFloatArray()

    private val mProgram = createProgram()
    private val vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(VERTEX_COUNT * BYRES_PER_VERTEX).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(triangleCoords)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        // get handle to shape's transformation matrix
        val vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, GL_MATRIX_VAR)
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

        // get handle to vertex shader's vPosition member
        val positionHandle = GLES20.glGetAttribLocation(mProgram, GL_POSITION_VAR)
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle)
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
            positionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            BYRES_PER_VERTEX,
            vertexBuffer
        )

        mProgram.setGLColor(color)

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTEX_COUNT)
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)
    }
}
