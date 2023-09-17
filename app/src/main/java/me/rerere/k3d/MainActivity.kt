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
import androidx.compose.foundation.layout.fillMaxSize
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
import me.rerere.k3d.controller.OrbitController
import me.rerere.k3d.loader.GltfLoader
import me.rerere.k3d.loader.ctx
import me.rerere.k3d.renderer.GLES3Renderer
import me.rerere.k3d.renderer.GLESAutoConfigChooser
import me.rerere.k3d.renderer.ViewportSize
import me.rerere.k3d.scene.Scene
import me.rerere.k3d.scene.actor.Primitive
import me.rerere.k3d.scene.camera.PerspectiveCamera
import me.rerere.k3d.scene.geometry.CubeGeometry
import me.rerere.k3d.scene.material.StandardMaterial
import me.rerere.k3d.ui.theme.K3dTheme
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.rotation.Euler
import me.rerere.k3d.util.math.rotation.toRadian
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.time.TimeSource

class MainActivity : ComponentActivity() {
    private val render = GLES3Renderer()
    private val camera = PerspectiveCamera().apply {
        position.set(0f, 0f, 5f)
    }
    private val cube = Primitive(
        geometry = CubeGeometry(),
        material = StandardMaterial(),
        count = 36
    ).apply {
        rotation.set(Euler(0f, 10f.toRadian(), 0f).toQuaternion())
    }
    private val scene = Scene().apply {
       // addChild(cube)
    }
    private lateinit var controls: OrbitController

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
                                val result = GltfLoader.load(
                                    inputStream = assets.open("sofa_combination.glb")
                                )
                                result.defaultScene.scale.set(0.2f, 0.2f, 0.2f)
                                scene.addChild(result.defaultScene)
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
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    AndroidView(
                        factory = { ctx ->
                            K3DView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                controls = OrbitController(
                                    camera,
                                    Vec3(),
                                    this
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
        private var previousTime = TimeSource.Monotonic.markNow()

        init {
            setEGLContextClientVersion(3)
            setEGLConfigChooser(GLESAutoConfigChooser)

            setRenderer(object : Renderer {
                override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                    println("Surface created")
                }

                override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                    println("Surface changed: $width, $height")
                    render.viewportSize = ViewportSize(width, height)
                    camera.aspect = width.toFloat() / height.toFloat()
                }

                override fun onDrawFrame(gl: GL10?) {
                    val deltaTime = previousTime.elapsedNow().inWholeMilliseconds.toFloat() / 1000f
                    previousTime = TimeSource.Monotonic.markNow()

                    val speed = 360f.toRadian() * deltaTime
                    //cube.rotation.applyRotation(Euler(0f, speed, 0f).toQuaternion())

                    render.render(scene, camera)
                }
            })
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            controls.handleEvent(event)
            return true
        }
    }
}