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
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.magnifier
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import me.rerere.k3d.renderer.shader.createProgram
import me.rerere.k3d.renderer.shader.createShader
import me.rerere.k3d.ui.theme.K3dTheme
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            K3dTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("K3D Demo") })
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            AndroidView(
                                factory = { ctx ->
                                    GLSurfaceView(ctx).apply {
                                        setEGLContextClientVersion(3)
                                        setRenderer(K3dRenderer(Shape.Triangle))

                                        layoutParams = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.MATCH_PARENT
                                        )
                                    }
                                },
                                modifier = Modifier.matchParentSize()
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AndroidView(
                                factory = { ctx ->
                                    GLSurfaceView(ctx).apply {
                                        setEGLContextClientVersion(3)
                                        setRenderer(K3dRenderer(Shape.Rectangle))

                                        layoutParams = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.MATCH_PARENT
                                        )
                                    }
                                },
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

class K3DView(context: Context?, attrs: AttributeSet?, shape: Shape) : GLSurfaceView(context, attrs) {
    init {
        setEGLContextClientVersion(3)
        setRenderer(K3dRenderer(shape))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return true
    }
}

enum class Shape {
    Rectangle,
    Triangle,
}

class K3dRenderer(private val shape: Shape) : Renderer {
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        println("Surface created")
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        println("Surface changed: $width, $height")
        GLES30.glViewport(0, 0, width, height)

        SystemClock.uptimeMillis()
    }

    override fun onDrawFrame(gl: GL10) {
        println(Thread.currentThread().name)

        GLES31.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        GLES31.glClearColor(0f, 0f, 0f, 1f)

        val vbos = intArrayOf(0)
        GLES31.glGenBuffers(1, vbos, 0)
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vbos[0])
        val data = when(shape) {
            Shape.Rectangle -> floatArrayOf(
                -0.5f, -0.5f, 0f,
                0.5f, -0.5f, 0f,
                0.5f, 0.5f, 0f,

                -0.5f, -0.5f, 0f,
                0.5f, 0.5f, 0f,
                -0.5f, 0.5f, 0f,
            )
            Shape.Triangle -> floatArrayOf(
                0.0f, 0.5f, 0f,
                -0.5f, -0.5f, 0f,
                0.5f, -0.5f, 0f,
            )
        }
        GLES31.glBufferData(
            GLES31.GL_ARRAY_BUFFER,
            data.size * 4,
            FloatBuffer.wrap(data),
            GLES31.GL_STATIC_DRAW
        )

        val program = createProgram(
            createShader(
                GLES31.GL_VERTEX_SHADER,
                """
                    #version 300 es
                    layout (location = 0) in vec3 aPos;
                    out vec4 vertexColor;
                    void main() {
                        gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
                        vertexColor = vec4(clamp(aPos, 0.0, 1.0), 1.0);
                    }
                """.trimIndent()
            ),
            createShader(
                GLES31.GL_FRAGMENT_SHADER,
                """
                    #version 300 es
                    precision mediump float;
                    
                    in vec4 vertexColor;
                    out vec4 FragColor;
                    
                    void main() {
                        FragColor = vertexColor;
                    }
                """.trimIndent()
            )
        )
        GLES30.glUseProgram(program)

        GLES31.glEnableVertexAttribArray(0)
        GLES31.glVertexAttribPointer(0, 3, GLES31.GL_FLOAT, false, 0, 0)

        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, data.size / 3)

        GLES31.glDisableVertexAttribArray(0)
        GLES31.glDeleteBuffers(1, vbos, 0)
        GLES31.glDeleteProgram(program)
    }
}