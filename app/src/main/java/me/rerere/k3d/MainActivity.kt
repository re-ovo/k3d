package me.rerere.k3d

import android.content.Context
import android.graphics.PixelFormat
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
import me.rerere.k3d.renderer.Clock
import me.rerere.k3d.renderer.GLES3Renderer
import me.rerere.k3d.renderer.GLESAutoConfigChooser
import me.rerere.k3d.renderer.ViewportSize
import me.rerere.k3d.renderer.resource.Uniform
import me.rerere.k3d.scene.actor.Mesh
import me.rerere.k3d.scene.actor.Scene
import me.rerere.k3d.scene.camera.PerspectiveCamera
import me.rerere.k3d.scene.geometry.CubeGeometry
import me.rerere.k3d.scene.light.AmbientLight
import me.rerere.k3d.scene.light.DirectionalLight
import me.rerere.k3d.scene.material.CookTorranceMaterial
import me.rerere.k3d.ui.theme.K3dTheme
import me.rerere.k3d.util.Color
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.rotation.Euler
import me.rerere.k3d.util.math.rotation.toRadian
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.time.TimeSource

class MainActivity : ComponentActivity() {
    private val render = GLES3Renderer()
    private val camera = PerspectiveCamera().apply {
        position.set(0f, 5f, 5f)
    }
    private val cubeMaterial = CookTorranceMaterial().apply {
        baseColor = Color(1f, 0f, 0f)
        roughness = 0.5f
        metallic = 0.5f
    }
    private val cube = Mesh(
        geometry = CubeGeometry(
            depth = 0.1f,
            height = 0.1f,
            width = 0.1f
        ),
        material = cubeMaterial,
        count = 36
    ).apply {
        // position.set(3f, 3f, 3f)
        position.set(0f, 0f, 0f)
        rotation.set(Euler(0f, 10f.toRadian(), 0f).toQuaternion())
    }
    private var model: Scene? = null
    private val ambientLight = AmbientLight(
        color = Color.fromRGBHex("#ffffff"),
        intensity = 0.1f
    )
    private val directionalLight = DirectionalLight(
        color = Color.fromRGBHex("#ffffff"),
        intensity = 1.9f,
        target = Vec3(0f, 0f, 0f)
    ).apply {
        position.set(30f, 30f, 30f)
    }
    private val scene = Scene().apply {
        addChild(cube)
        addChild(ambientLight)
        addChild(directionalLight)
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
                                val result = GltfLoader(this@MainActivity).load(
                                    inputStream = assets.open("free_isometric_cafe.glb")
                                )
                                //result.defaultScene.scale.set(0.1f, 0.1f, 0.1f)
                                scene.addChild(result.defaultScene)
                                model = result.defaultScene
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
                        .weight(1f)
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

                K3DController {
                    K3DFloatController(
                        label = "Directional Light",
                        getter = { directionalLight.intensity },
                        setter = {
                            directionalLight.intensity = it
                        },
                        max = 15.0f
                    )

                    K3DFloatController(
                        label = "Ambient Light",
                        getter = { ambientLight.intensity },
                        setter = {
                            ambientLight.intensity = it
                        },
                    )

                    K3DFloatController(
                        label = "Cube Color (R)",
                        getter = { cubeMaterial.baseColor.r },
                        setter = {
                            cubeMaterial.baseColor = cubeMaterial.baseColor.copy(r = it)
                        },
                        max = 1f
                    )

                    K3DFloatController(
                        label = "Roughness",
                        getter = { cubeMaterial.roughness },
                        setter = {
                            cubeMaterial.roughness = it
                        },
                    )

                    K3DFloatController(
                        label = "Metallic",
                        getter = { cubeMaterial.metallic },
                        setter = {
                            cubeMaterial.metallic = it
                        },
                    )
                }
            }
        }
    }

    inner class K3DView(context: Context?, attrs: AttributeSet? = null) :
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
                }

                override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                    println("Surface changed: $width, $height")
                    render.viewportSize = ViewportSize(width, height)
                    camera.aspect = width.toFloat() / height.toFloat()
                }

                override fun onDrawFrame(gl: GL10?) {
                    clock.tick()

                    val speed = 360f.toRadian() * clock.getDelta()
                    // model?.rotation?.applyRotation(Euler(0f, speed, 0f).toQuaternion())

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