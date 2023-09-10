package me.rerere.k3d

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLES31
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import android.os.Bundle
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.viewinterop.AndroidView
import me.rerere.k3d.renderer.shader.createProgram
import me.rerere.k3d.renderer.shader.createShader
import me.rerere.k3d.ui.theme.K3dTheme
import java.nio.FloatBuffer
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
        GLES31.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        GLES31.glClearColor(0f, 0f, 0f, 1f)

        val vbos = intArrayOf(0)
        GLES31.glGenBuffers(1, vbos, 0)
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vbos[0])
        val data = floatArrayOf(
            -0.5f, -0.5f, 0f,
            0.5f, -0.5f, 0f,
            0f, 0.5f, 0f
        )
        GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, data.size * 4, FloatBuffer.wrap(data), GLES31.GL_STATIC_DRAW)

        val program = createProgram(
            createShader(
                GLES31.GL_VERTEX_SHADER,
                """
                    #version 300 es
                    layout (location = 0) in vec3 aPos;
                    void main() {
                        gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
                    }
                """.trimIndent()
            ),
            createShader(
                GLES31.GL_FRAGMENT_SHADER,
                """
                    #version 300 es
                    precision mediump float;
                    out vec4 FragColor;
                    void main() {
                        FragColor = vec4(1.0, 0.5, 0.2, 1.0);
                    }
                """.trimIndent()
            )
        )
        GLES31.glUseProgram(program)

        GLES31.glEnableVertexAttribArray(0)
        GLES31.glVertexAttribPointer(0, 3, GLES31.GL_FLOAT, false, 0, 0)

        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, 3)
    }
}