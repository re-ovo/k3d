package me.rerere.k3d

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import me.rerere.k3d.loader.GltfLoader
import me.rerere.k3d.renderer.GLES3Renderer
import me.rerere.k3d.renderer.ViewportSize
import me.rerere.k3d.scene.Scene
import me.rerere.k3d.scene.camera.PerspectiveCamera
import me.rerere.k3d.ui.theme.K3dTheme
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : ComponentActivity() {
    private val render = GLES3Renderer()
    private val camera = PerspectiveCamera()
    private val scene = Scene()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            K3dTheme {
                Home()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Home() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("K3D Demo") },
                    actions = {
                        TextButton(
                            onClick = {
                                GltfLoader.load(
                                    inputStream = assets.open("sofa_combination.glb")
                                )
                            }
                        ) {
                            Text("Load")
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
                    modifier = Modifier.aspectRatio(16/9f).fillMaxWidth()
                ) {
                    AndroidView(
                        factory = { ctx ->
                            K3DView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }
    }

    inner class K3DView(context: Context?, attrs: AttributeSet? = null) :
        GLSurfaceView(context, attrs) {
        init {
            setEGLContextClientVersion(3)
            setRenderer(object : Renderer {
                override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                    println("Surface created")
                }

                override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                    println("Surface changed: $width, $height")
                    render.viewportSize = ViewportSize(width, height)
                }

                override fun onDrawFrame(gl: GL10?) {
                    render.render(scene, camera)
                }
            })
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            return true
        }
    }
}