package me.rerere.k3d

import android.content.Context
import android.opengl.GLES30
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import me.rerere.k3d.renderer.GL3Renderer
import me.rerere.k3d.renderer.shader.createProgram
import me.rerere.k3d.renderer.shader.createShader
import me.rerere.k3d.renderer.shader.genBuffer
import me.rerere.k3d.renderer.shader.genVertexArray
import me.rerere.k3d.scene.Scene
import me.rerere.k3d.scene.camera.Camera
import me.rerere.k3d.scene.camera.DummyCamera
import me.rerere.k3d.ui.theme.K3dTheme
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : ComponentActivity() {
    private val secondRender = K3dRenderer(Shape.Rectangle)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            K3dTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("K3D Demo") },
                            actions = {
                                TextButton(
                                    onClick = {
                                        secondRender._render.moveTest()
                                    }
                                ) {
                                    Text("Move")
                                }
                            }
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                    ) {
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
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
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    GLSurfaceView(ctx).apply {
                                        setEGLContextClientVersion(3)
                                        setRenderer(secondRender)

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

class K3DView(context: Context?, attrs: AttributeSet?, shape: Shape) :
    GLSurfaceView(context, attrs) {
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
    val _render = GL3Renderer()
    private val _dummyScene = Scene()
    private val _camera = DummyCamera

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        println("Surface created")
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        println("Surface changed: $width, $height")
        _render.resize(width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        _render.render(_dummyScene, _camera)
    }
}