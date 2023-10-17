package me.rerere.k3d.ui

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import me.rerere.k3d.renderer.Clock
import me.rerere.k3d.renderer.GLESAutoConfigChooser
import me.rerere.k3d.renderer.shader.glGetIntegerv
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class K3DView(
    context: Context?,
    val onSurfaceChanged: (GL10?, Int, Int) -> Unit,
    val onDrawFrame: (GL10?) -> Unit,
    val onTouch: (MotionEvent) -> Unit,
    attrs: AttributeSet? = null
) :
    GLSurfaceView(context, attrs) {
    private val clock = Clock()

    init {
        setEGLContextClientVersion(3)
        setEGLConfigChooser(GLESAutoConfigChooser)
        // holder.setFormat(PixelFormat.RGBA_8888)
        // setZOrderOnTop(true)

        setRenderer(object : Renderer {
            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                println("Surface created")
                println(
                    "MAX_VERTEX_UNIFORM_VECTORS: ${
                        glGetIntegerv(GLES30.GL_MAX_VERTEX_UNIFORM_VECTORS)
                    }"
                )
                println(
                    "MAX_FRAGMENT_UNIFORM_VECTORS: ${
                        glGetIntegerv(GLES30.GL_MAX_FRAGMENT_UNIFORM_VECTORS)
                    }"
                )
                println(
                    "MAX_VERTEX_ATTRIBS: ${
                        glGetIntegerv(GLES30.GL_MAX_VERTEX_ATTRIBS)
                    }"
                )
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                println("Surface changed: $width, $height")
                this@K3DView.onSurfaceChanged(gl, width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                this@K3DView.onDrawFrame(gl)
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        this.onTouch(event)
        return true
    }
}