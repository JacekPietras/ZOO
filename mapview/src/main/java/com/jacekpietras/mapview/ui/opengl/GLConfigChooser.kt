package com.jacekpietras.mapview.ui.opengl

import android.opengl.GLSurfaceView.EGLConfigChooser
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay


internal class GLConfigChooser : EGLConfigChooser {

    override fun chooseConfig(egl: EGL10?, display: EGLDisplay?): EGLConfig? {
        val attrs = intArrayOf(
            EGL10.EGL_LEVEL, 0,
            EGL10.EGL_RENDERABLE_TYPE, 4,  // EGL_OPENGL_ES2_BIT
            EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_DEPTH_SIZE, 16,
            EGL10.EGL_SAMPLE_BUFFERS, 1,
            EGL10.EGL_SAMPLES, 4,  // This is for 4x MSAA.
            EGL10.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val configCounts = IntArray(1)
        egl?.eglChooseConfig(display, attrs, configs, 1, configCounts)
        return if (configCounts[0] == 0) {
            // Failed! Error handling.
            null
        } else {
            configs[0]
        }
    }
}
