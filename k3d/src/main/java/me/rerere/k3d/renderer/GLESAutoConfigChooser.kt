package me.rerere.k3d.renderer

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

/**
 * Choose EGLConfig automatically
 *
 * Prefer 24 bit depth buffer
 */
object GLESAutoConfigChooser : GLSurfaceView.EGLConfigChooser {
    override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
        val attributes = intArrayOf(
            EGL10.EGL_DEPTH_SIZE, 24,
            EGL10.EGL_NONE
        )

        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        egl.eglChooseConfig(display, attributes, configs, 1, numConfigs)

        if (numConfigs[0] == 0) {
            // There is no EGLConfig that satisfies the criteria.
            attributes[1] = 16
            egl.eglChooseConfig(display, attributes, configs, 1, numConfigs)
        }

        if (numConfigs[0] == 0) {
            throw RuntimeException("No configs match configSpec")
        } else {
            return configs[0]!!
        }
    }
}