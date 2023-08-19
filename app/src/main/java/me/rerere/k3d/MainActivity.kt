package me.rerere.k3d

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix
import android.os.Bundle
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.viewinterop.AndroidView
import me.rerere.k3d.ui.theme.K3dTheme
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            K3dTheme {
                AndroidView(factory = { ctx ->
                    GLSurfaceView(ctx).apply {
                        setEGLContextClientVersion(3)
                        setRenderer(K3dRenderer())
                    }
                })
            }
        }
    }
}

class K3DView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
    init {
        setEGLContextClientVersion(3)
        setRenderer(K3dRenderer())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return true
    }
}

class K3dRenderer: Renderer {
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        println("Surface created")
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        println("Surface changed: $width, $height")
        GLES30.glViewport(0, 0, width, height)

        SystemClock.uptimeMillis()
    }

    override fun onDrawFrame(gl: GL10) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        gl.glClearColor(1f, 0f, 0f, 1f)
    }
}